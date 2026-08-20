@file:OptIn(com.github.michaelbull.result.annotation.UnsafeResultValueAccess::class)

package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchCredential
import ch.admin.foitt.openid4vc.domain.model.BatchCredentialItem
import ch.admin.foitt.openid4vc.domain.model.CredentialType
import ch.admin.foitt.openid4vc.domain.model.DeferredCredential
import ch.admin.foitt.openid4vc.domain.model.FetchCredentialResult
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateCredentialRequestError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateDPoPProofJwtError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchAccessTokenError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchNonceError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.IssuerNonce
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.TokenResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryption
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequest
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequestProofsJwt
import ch.admin.foitt.openid4vc.domain.usecase.CreateDPoPProofJwt
import ch.admin.foitt.openid4vc.domain.usecase.DeleteKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.utils.retryUseCase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThenRecover
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import java.net.URL
import javax.inject.Inject

internal class FetchVerifiableCredentialImpl @Inject constructor(
    private val credentialOfferRepository: CredentialOfferRepository,
    private val createDPoPProofJwt: CreateDPoPProofJwt,
    private val createCredentialRequestProofsJwt: CreateCredentialRequestProofsJwt,
    private val createCredentialRequest: CreateCredentialRequest,
    private val deleteKeyPair: DeleteKeyPair,
) : FetchVerifiableCredential {
    override suspend fun invoke(
        verifiableCredentialParams: VerifiableCredentialParams,
        credentialBindingKeyPairs: List<BindingKeyPair>?,
        dpopKeyPair: BindingKeyPair?,
        payloadEncryption: PayloadEncryption,
    ): Result<FetchCredentialResult, FetchVerifiableCredentialError> = coroutineBinding {
        val isDpop = verifiableCredentialParams.dpopSigningAlgValuesSupported != null

        if (isDpop) {
            if (dpopKeyPair == null) {
                Err(CredentialOfferError.Unexpected(IllegalStateException("DPoP key pair is null"))).bind()
            }
            fetchCredentialUsingDpop(
                verifiableCredentialParams = verifiableCredentialParams,
                credentialBindingKeyPairs = credentialBindingKeyPairs,
                dpopKeyPair = dpopKeyPair,
                payloadEncryption = payloadEncryption,
            ).bind()
        } else {
            fetchCredential(
                verifiableCredentialParams = verifiableCredentialParams,
                credentialBindingKeyPairs = credentialBindingKeyPairs,
                payloadEncryptionType = payloadEncryption,
            ).bind()
        }
    }

    private suspend fun fetchCredentialUsingDpop(
        verifiableCredentialParams: VerifiableCredentialParams,
        credentialBindingKeyPairs: List<BindingKeyPair>?,
        dpopKeyPair: BindingKeyPair,
        payloadEncryption: PayloadEncryption
    ): Result<FetchCredentialResult, FetchVerifiableCredentialError> {
        return coroutineBinding {
            val tokenRequestNonce = fetchIssuerNonce(verifiableCredentialParams.nonceEndpoint).bind()
            val tokenResponse = getToken(
                verifiableCredentialParams = verifiableCredentialParams,
                dpopKeyPair = dpopKeyPair,
                dpopNonce = tokenRequestNonce.dpopNonce,
            ).bind()

            val credentialNonce = fetchIssuerNonce(verifiableCredentialParams.nonceEndpoint).bind()
            val proofs = credentialBindingKeyPairs?.let {
                retryUseCase {
                    createCredentialRequestProofsJwt(
                        keyPairs = credentialBindingKeyPairs,
                        issuer = verifiableCredentialParams.issuerEndpoint.toString(),
                        cNonce = credentialNonce.cNonce,
                    )
                }.bind()
            }

            val request = createCredentialRequest(
                payloadEncryption = payloadEncryption,
                credentialType = CredentialType.Verifiable(
                    verifiableCredentialParams = verifiableCredentialParams,
                    proofs = proofs,
                )
            ).mapError(CreateCredentialRequestError::toFetchVerifiableCredentialError)
                .bind()

            val credentialDpopProof = createCredentialDpopProof(
                verifiableCredentialParams = verifiableCredentialParams,
                dpopKeyPair = dpopKeyPair,
                dpopNonce = credentialNonce.dpopNonce,
                accessToken = tokenResponse.accessToken,
            ).bind()

            val fetchCredentialResponse = credentialOfferRepository.fetchCredential(
                issuerEndpoint = verifiableCredentialParams.credentialEndpoint,
                tokenResponse = tokenResponse,
                request = request,
                payloadEncryption = payloadEncryption,
                dpopProof = credentialDpopProof,
            ).andThenRecover { error ->
                if (error is CredentialOfferError.UseDPoPNonce) {
                    val retriedDpopProof = createCredentialDpopProof(
                        verifiableCredentialParams = verifiableCredentialParams,
                        dpopKeyPair = dpopKeyPair,
                        dpopNonce = error.nonce,
                        accessToken = tokenResponse.accessToken,
                    ).bind()
                    credentialOfferRepository.fetchCredential(
                        issuerEndpoint = verifiableCredentialParams.credentialEndpoint,
                        tokenResponse = tokenResponse,
                        request = request,
                        payloadEncryption = payloadEncryption,
                        dpopProof = retriedDpopProof,
                    )
                } else {
                    Err(error)
                }
            }.bind()

            getCredentialResult(
                credentialResponse = fetchCredentialResponse,
                verifiableCredentialParams = verifiableCredentialParams,
                bindingKeyPairs = credentialBindingKeyPairs,
                tokenResponse = tokenResponse,
                dpopKeyPair = dpopKeyPair,
            ).bind()
        }.onErr {
            deleteHardwareKeys(
                bindingKeyPairs = (credentialBindingKeyPairs?.plus(dpopKeyPair)),
            )
        }
    }

    private suspend fun fetchCredential(
        verifiableCredentialParams: VerifiableCredentialParams,
        credentialBindingKeyPairs: List<BindingKeyPair>?,
        payloadEncryptionType: PayloadEncryption
    ): Result<FetchCredentialResult, FetchVerifiableCredentialError> = coroutineBinding {
        val tokenResponse = getToken(verifiableCredentialParams, null, null)
            .bind()
        val proofs = credentialBindingKeyPairs?.let {
            val nonce = credentialOfferRepository.fetchNonce(verifiableCredentialParams.nonceEndpoint)
                .mapError(FetchNonceError::toFetchVerifiableCredentialError)
                .bind()
            retryUseCase {
                createCredentialRequestProofsJwt(
                    keyPairs = credentialBindingKeyPairs,
                    issuer = verifiableCredentialParams.issuerEndpoint.toString(),
                    cNonce = nonce.cNonce,
                )
            }.bind()
        }
        val credentialRequestType = createCredentialRequest(
            payloadEncryption = payloadEncryptionType,
            credentialType = CredentialType.Verifiable(
                verifiableCredentialParams = verifiableCredentialParams,
                proofs = proofs,
            )
        ).mapError(CreateCredentialRequestError::toFetchVerifiableCredentialError)
            .bind()

        val fetchCredentialResponse = credentialOfferRepository.fetchCredential(
            issuerEndpoint = verifiableCredentialParams.credentialEndpoint,
            tokenResponse = tokenResponse,
            request = credentialRequestType,
            payloadEncryption = payloadEncryptionType,
        ).bind()

        getCredentialResult(
            credentialResponse = fetchCredentialResponse,
            verifiableCredentialParams = verifiableCredentialParams,
            bindingKeyPairs = credentialBindingKeyPairs,
            tokenResponse = tokenResponse,
            dpopKeyPair = null,
        ).bind()
    }.onErr { deleteHardwareKeys(credentialBindingKeyPairs) }

    private suspend fun getToken(
        verifiableCredentialParams: VerifiableCredentialParams,
        dpopKeyPair: BindingKeyPair?,
        dpopNonce: String?,
    ): Result<TokenResponse, FetchVerifiableCredentialError> = coroutineBinding {
        val tokenEndpoint = verifiableCredentialParams.tokenEndpoint
        val grant = verifiableCredentialParams.grants

        when {
            grant.preAuthorizedCode != null -> {
                val initialProof = createTokenDpopProof(
                    tokenEndpoint = tokenEndpoint,
                    dpopKeyPair = dpopKeyPair,
                    dpopNonce = dpopNonce,
                ).bind()
                credentialOfferRepository.fetchAccessToken(
                    tokenEndpoint = tokenEndpoint,
                    preAuthorizedCode = grant.preAuthorizedCode.preAuthorizedCode,
                    dpopProof = initialProof,
                ).andThenRecover { error ->
                    if (error is CredentialOfferError.UseDPoPNonce) {
                        val retriedProof = createTokenDpopProof(
                            tokenEndpoint = tokenEndpoint,
                            dpopKeyPair = dpopKeyPair,
                            dpopNonce = error.nonce,
                        ).bind()
                        credentialOfferRepository.fetchAccessToken(
                            tokenEndpoint = tokenEndpoint,
                            preAuthorizedCode = grant.preAuthorizedCode.preAuthorizedCode,
                            dpopProof = retriedProof,
                        )
                    } else {
                        Err(error)
                    }
                }.mapError(FetchAccessTokenError::toFetchVerifiableCredentialError)
            }

            grant.refreshToken != null -> {
                val initialProof = createTokenDpopProof(
                    tokenEndpoint = tokenEndpoint,
                    dpopKeyPair = dpopKeyPair,
                    dpopNonce = dpopNonce,
                ).bind()
                credentialOfferRepository.fetchAccessTokenByRefreshToken(
                    tokenEndpoint = tokenEndpoint,
                    refreshToken = grant.refreshToken,
                    dpopProof = initialProof,
                ).andThenRecover { error ->
                    if (error is CredentialOfferError.UseDPoPNonce) {
                        val retriedProof = createTokenDpopProof(
                            tokenEndpoint = tokenEndpoint,
                            dpopKeyPair = dpopKeyPair,
                            dpopNonce = error.nonce,
                        ).bind()
                        credentialOfferRepository.fetchAccessTokenByRefreshToken(
                            tokenEndpoint = tokenEndpoint,
                            refreshToken = grant.refreshToken,
                            dpopProof = retriedProof,
                        )
                    } else {
                        Err(error)
                    }
                }.mapError(FetchAccessTokenError::toFetchVerifiableCredentialError)
            }

            else -> {
                Err(CredentialOfferError.UnsupportedGrantType)
            }
        }.bind()
    }

    private suspend fun getCredentialResult(
        credentialResponse: CredentialResponse,
        verifiableCredentialParams: VerifiableCredentialParams,
        bindingKeyPairs: List<BindingKeyPair>?,
        tokenResponse: TokenResponse,
        dpopKeyPair: BindingKeyPair?,
    ): Result<FetchCredentialResult, FetchVerifiableCredentialError> = coroutineBinding {
        val keyBindings = bindingKeyPairs?.map { getKeyBinding(it.keyPair) }
        val dpopKeyBinding = dpopKeyPair?.let { getKeyBinding(it.keyPair) }
        when (credentialResponse) {
            is CredentialResponse.VerifiableCredential -> {
                if (verifiableCredentialParams.isBatch) {
                    BatchCredential(
                        accessToken = tokenResponse.accessToken,
                        refreshToken = tokenResponse.refreshToken,
                        dpopKeyBinding = dpopKeyBinding,
                        credentials = credentialResponse.credentials.mapIndexed { index, credential ->
                            BatchCredentialItem(
                                credential.credential,
                                keyBinding = keyBindings?.getOrNull(index),
                            )
                        },
                    )
                } else {
                    VerifiableCredential(
                        accessToken = tokenResponse.accessToken,
                        refreshToken = tokenResponse.refreshToken,
                        dpopKeyBinding = dpopKeyBinding,
                        credential = credentialResponse.credentials.first().credential,
                        keyBinding = keyBindings?.first(),
                    )
                }
            }

            is CredentialResponse.DeferredCredential -> {
                if (verifiableCredentialParams.deferredCredentialEndpoint != null) {
                    DeferredCredential(
                        transactionId = credentialResponse.transactionId,
                        pollInterval = credentialResponse.interval,
                        format = verifiableCredentialParams.credentialConfiguration.format,
                        keyBindings = keyBindings,
                        accessToken = tokenResponse.accessToken,
                        tokenType = tokenResponse.tokenType,
                        refreshToken = tokenResponse.refreshToken,
                        endpoint = verifiableCredentialParams.deferredCredentialEndpoint,
                        dpopKeyBinding = dpopKeyBinding,
                    )
                } else {
                    Err(CredentialOfferError.MetadataMisconfiguration("Missing deferred credential endpoint")).bind()
                }
            }
        }
    }

    private fun getKeyBinding(keyPair: JWSKeyPair): KeyBinding {
        val (publicKey, privateKey) = if (keyPair.bindingType == KeyBindingType.SOFTWARE) {
            keyPair.keyPair.public.encoded to keyPair.keyPair.private.encoded
        } else {
            null to null
        }
        return KeyBinding(
            identifier = keyPair.keyId,
            algorithm = keyPair.algorithm,
            bindingType = keyPair.bindingType,
            publicKey = publicKey,
            privateKey = privateKey
        )
    }

    private suspend fun deleteHardwareKeys(
        bindingKeyPairs: List<BindingKeyPair>?,
    ) {
        bindingKeyPairs?.forEach { bindingKeyPair ->
            val keyPair = bindingKeyPair.keyPair
            if (keyPair.bindingType == KeyBindingType.HARDWARE) {
                deleteKeyPair(keyPair.keyId)
            }
        }
    }

    private suspend fun fetchIssuerNonce(nonceEndpoint: URL): Result<IssuerNonce, FetchVerifiableCredentialError> =
        credentialOfferRepository.fetchNonce(nonceEndpoint)
            .mapError(FetchNonceError::toFetchVerifiableCredentialError)

    private suspend fun createTokenDpopProof(
        tokenEndpoint: URL,
        dpopKeyPair: BindingKeyPair?,
        dpopNonce: String?,
    ): Result<String?, FetchVerifiableCredentialError> = coroutineBinding {
        dpopKeyPair?.let {
            createDPoPProofJwt(
                method = "POST",
                url = tokenEndpoint,
                keyPair = it.keyPair,
                nonce = dpopNonce,
                accessToken = null,
                requestBody = null,
                keyAttestationJwt = it.attestationJwt,
            ).mapError(CreateDPoPProofJwtError::toFetchVerifiableCredentialError).bind()
        }
    }

    private suspend fun createCredentialDpopProof(
        verifiableCredentialParams: VerifiableCredentialParams,
        dpopKeyPair: BindingKeyPair,
        dpopNonce: String?,
        accessToken: String,
    ): Result<String?, FetchVerifiableCredentialError> = coroutineBinding {
        createDPoPProofJwt(
            method = "POST",
            url = verifiableCredentialParams.credentialEndpoint,
            keyPair = dpopKeyPair.keyPair,
            nonce = dpopNonce,
            accessToken = accessToken,
            requestBody = null,
            keyAttestationJwt = null,
        ).mapError(CreateDPoPProofJwtError::toFetchVerifiableCredentialError).bind()
    }
}
