package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.CredentialType
import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateCredentialRequestError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateDPoPProofJwtError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchAccessTokenError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchDeferredCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerConfigurationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchNonceError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetSignedMetadataDidError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.TokenResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryption
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequest
import ch.admin.foitt.openid4vc.domain.usecase.CreateDPoPProofJwt
import ch.admin.foitt.openid4vc.domain.usecase.FetchIssuerConfiguration
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GetSignedMetadataDid
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchAndUpdateDeferredCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.SaveCredentialFromDeferredError
import ch.admin.foitt.wallet.platform.credential.domain.model.UpdateDeferredCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchAndUpdateDeferredCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toUpdateDeferredCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndUpdateDeferredCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredentialFromDeferred
import ch.admin.foitt.wallet.platform.credential.domain.usecase.UpdateDeferredCredential
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.GetBindingKeyPairError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.GetBindingKeyPair
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithAuthenticationAndKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryption
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeferredCredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThenRecover
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import timber.log.Timber
import java.net.URL
import java.time.Instant
import javax.inject.Inject
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository as OIDCredentialOfferRepository

internal class FetchAndUpdateDeferredCredentialImpl @Inject constructor(
    private val deferredCredentialRepository: DeferredCredentialRepository,
    private val fetchRawAndParsedIssuerCredentialInfo: FetchRawAndParsedIssuerCredentialInfo,
    private val getPayloadEncryption: GetPayloadEncryption,
    private val createCredentialRequest: CreateCredentialRequest,
    private val getSignedMetadataDid: GetSignedMetadataDid,
    private val processIdentityTrustStatement: ProcessIdentityTrustStatement,
    private val createDPoPProofJwt: CreateDPoPProofJwt,
    private val getBindingKeyPair: GetBindingKeyPair,
    private val oidCredentialOfferRepository: OIDCredentialOfferRepository,
    private val saveCredentialFromDeferred: SaveCredentialFromDeferred,
    private val updateDeferredCredential: UpdateDeferredCredential,
    private val fetchIssuerConfiguration: FetchIssuerConfiguration,
) : FetchAndUpdateDeferredCredential {
    override suspend operator fun invoke(
        deferredCredentialEntity: DeferredCredentialWithAuthenticationAndKeyBinding,
    ): Result<Unit, FetchAndUpdateDeferredCredentialError> = coroutineBinding {
        val deferredEntity = deferredCredentialEntity.deferredCredential
        val rawAndParsedIssuerCredentialInfo = fetchRawAndParsedIssuerCredentialInfo(
            issuerEndpoint = deferredCredentialEntity.credential.issuerUrl,
            forceRefresh = true,
        ).mapError(FetchIssuerCredentialInfoError::toFetchAndUpdateDeferredCredentialError)
            .bind()

        val payloadEncryption = getPayloadEncryption(
            requestEncryption = rawAndParsedIssuerCredentialInfo.issuerCredentialInfo.credentialRequestEncryption,
            responseEncryption = rawAndParsedIssuerCredentialInfo.issuerCredentialInfo.credentialResponseEncryption,
        ).mapError(GetPayloadEncryptionTypeError::toFetchAndUpdateDeferredCredentialError)
            .bind()

        val request = createCredentialRequest(
            payloadEncryption = payloadEncryption,
            credentialType = CredentialType.Deferred(transactionId = deferredEntity.transactionId)
        ).mapError(CreateCredentialRequestError::toFetchAndUpdateDeferredCredentialError)
            .bind()

        val actorDid = getSignedMetadataDid(rawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo)
            .mapError(GetSignedMetadataDidError::toFetchAndUpdateDeferredCredentialError)
            .bind()
        val identityV2TrustStatement = processIdentityTrustStatement(
            identityTrustStatementJwt = rawAndParsedIssuerCredentialInfo.issuerCredentialInfo.identityTrustStatement,
            actorDid = actorDid
        ).mapError(ProcessIdentityTrustStatementError::toFetchAndUpdateDeferredCredentialError)
            .bind()

        val credentialResponse = tryFetchDeferredCredentialWithRefreshToken(
            deferredCredentialEntity = deferredCredentialEntity,
            issuerCredentialInfo = rawAndParsedIssuerCredentialInfo.issuerCredentialInfo,
            request = request,
            payloadEncryption = payloadEncryption,
        ).onErr { error ->
            handleDeferredError(
                error = error,
                deferredCredential = deferredCredentialEntity,
            )
        }.mapError(UpdateDeferredCredentialError::toFetchAndUpdateDeferredCredentialError).bind()

        when (credentialResponse) {
            is CredentialResponse.DeferredCredential -> updateDeferredCredential(
                deferredCredentialEntity = deferredCredentialEntity,
                credentialResponse = credentialResponse,
                rawAndParsedIssuerCredentialInfo = rawAndParsedIssuerCredentialInfo,
            ).onErr {
                Timber.e(message = "Deferred refresh: update deferred failed")
            }

            is CredentialResponse.VerifiableCredential -> saveCredentialFromDeferred(
                deferredCredentialEntity = deferredCredentialEntity,
                credentialResponse = credentialResponse,
                rawAndParsedIssuerCredentialInfo = rawAndParsedIssuerCredentialInfo,
                identityV2TrustStatement = identityV2TrustStatement,
            ).onErr { error ->
                handleCredentialError(
                    deferredCredentialEntity = deferredCredentialEntity,
                    error = error,
                )
            }
        }
    }

    private suspend fun tryFetchDeferredCredentialWithRefreshToken(
        deferredCredentialEntity: DeferredCredentialWithAuthenticationAndKeyBinding,
        issuerCredentialInfo: IssuerCredentialInfo,
        request: String,
        payloadEncryption: PayloadEncryption,
    ): Result<CredentialResponse, UpdateDeferredCredentialError> = coroutineBinding {
        val deferredEntity = deferredCredentialEntity.deferredCredential
        val deferredCredentialEndpoint = deferredEntity.endpoint
        val credentialAuthentication = deferredCredentialEntity.authentication
        val fallbackNonce = oidCredentialOfferRepository.fetchNonce(issuerCredentialInfo.nonceEndpoint)
            .mapError(FetchNonceError::toUpdateDeferredCredentialError)
            .bind()
            .dpopNonce
        val dpopKeyPair = getBindingKeyPair(deferredCredentialEntity.authentication)
            .mapError(GetBindingKeyPairError::toUpdateDeferredCredentialError)
            .bind()

        val accessToken = credentialAuthentication.accessToken
        fetchDeferredCredential(
            issuerEndpoint = deferredCredentialEndpoint,
            request = request,
            payloadEncryption = payloadEncryption,
            dpopKeyPair = dpopKeyPair,
            fallbackNonce = fallbackNonce,
            accessToken = accessToken,
            tokenType = credentialAuthentication.tokenType,
        ).andThenRecover { error ->
            if (
                error is CredentialOfferError.InvalidToken &&
                credentialAuthentication.refreshToken != null
            ) {
                val refreshToken = credentialAuthentication.refreshToken ?: Err(CredentialError.InvalidCredentialOffer).bind()
                val issuerConfiguration = fetchIssuerConfiguration(deferredCredentialEntity.credential.issuerUrl)
                    .mapError(FetchIssuerConfigurationError::toUpdateDeferredCredentialError).bind()

                val initialProof = createDpopProof(
                    url = issuerConfiguration.tokenEndpoint.toString(),
                    keyPair = dpopKeyPair,
                    nonce = fallbackNonce,
                    accessToken = null,
                ).mapError(CreateDPoPProofJwtError::toUpdateDeferredCredentialError).bind()

                val tokenResponse: TokenResponse = oidCredentialOfferRepository.fetchAccessTokenByRefreshToken(
                    tokenEndpoint = issuerConfiguration.tokenEndpoint,
                    refreshToken = refreshToken,
                    dpopProof = initialProof,
                ).andThenRecover { tokenError ->
                    if (tokenError is CredentialOfferError.UseDPoPNonce) {
                        val retriedProof = createDpopProof(
                            url = issuerConfiguration.tokenEndpoint.toString(),
                            keyPair = dpopKeyPair,
                            nonce = tokenError.nonce,
                            accessToken = null,
                        ).mapError(CreateDPoPProofJwtError::toUpdateDeferredCredentialError).bind()
                        oidCredentialOfferRepository.fetchAccessTokenByRefreshToken(
                            tokenEndpoint = issuerConfiguration.tokenEndpoint,
                            refreshToken = refreshToken,
                            dpopProof = retriedProof,
                        )
                    } else {
                        Err(tokenError)
                    }
                }.mapError(FetchAccessTokenError::toUpdateDeferredCredentialError).bind()

                deferredCredentialRepository.updateTokens(
                    credentialId = deferredEntity.credentialId,
                    tokenResponse = tokenResponse,
                ).mapError(DeferredCredentialRepositoryError::toUpdateDeferredCredentialError).bind()

                fetchDeferredCredential(
                    issuerEndpoint = deferredCredentialEndpoint,
                    request = request,
                    payloadEncryption = payloadEncryption,
                    dpopKeyPair = dpopKeyPair,
                    fallbackNonce = fallbackNonce,
                    accessToken = tokenResponse.accessToken,
                    tokenType = tokenResponse.tokenType,
                )
            } else {
                Err(error)
            }
        }.mapError(FetchDeferredCredentialError::toUpdateDeferredCredentialError).bind()
    }

    private suspend fun fetchDeferredCredential(
        issuerEndpoint: String,
        request: String,
        payloadEncryption: PayloadEncryption,
        dpopKeyPair: BindingKeyPair?,
        fallbackNonce: String?,
        accessToken: String,
        tokenType: TokenType,
    ): Result<CredentialResponse, FetchDeferredCredentialError> = coroutineBinding {
        val initialProof = createDpopProof(
            url = issuerEndpoint,
            keyPair = dpopKeyPair,
            nonce = fallbackNonce,
            accessToken = accessToken,
        ).mapError { it as FetchDeferredCredentialError }.bind()

        oidCredentialOfferRepository.fetchDeferredCredential(
            issuerEndpoint = issuerEndpoint,
            tokenType = tokenType,
            accessToken = accessToken,
            request = request,
            payloadEncryption = payloadEncryption,
            dpopProof = initialProof,
        ).andThenRecover { error ->
            if (error is CredentialOfferError.UseDPoPNonce) {
                val nonce = error.nonce
                val retriedProof = createDpopProof(
                    url = issuerEndpoint,
                    keyPair = dpopKeyPair,
                    nonce = nonce,
                    accessToken = accessToken,
                ).mapError { it as FetchDeferredCredentialError }.bind()
                oidCredentialOfferRepository.fetchDeferredCredential(
                    issuerEndpoint = issuerEndpoint,
                    tokenType = tokenType,
                    accessToken = accessToken,
                    request = request,
                    payloadEncryption = payloadEncryption,
                    dpopProof = retriedProof,
                )
            } else {
                Err(error)
            }
        }.bind()
    }

    private suspend fun createDpopProof(
        url: String,
        keyPair: BindingKeyPair?,
        nonce: String?,
        accessToken: String?,
    ): Result<String?, CreateDPoPProofJwtError> = coroutineBinding {
        keyPair?.let {
            createDPoPProofJwt(
                method = "POST",
                url = URL(url),
                keyPair = keyPair.keyPair,
                nonce = nonce,
                accessToken = accessToken,
                keyAttestationJwt = null,
                requestBody = null,
            ).bind()
        }
    }

    private suspend fun handleDeferredError(
        error: UpdateDeferredCredentialError,
        deferredCredential: DeferredCredentialWithAuthenticationAndKeyBinding,
    ) {
        when (error) {
            CredentialError.InvalidCredentialOffer -> invalidateCredential(deferredCredential)
            CredentialError.IncompatibleDeviceKeyStorage,
            CredentialError.InvalidGenerateMetadataClaims,
            CredentialError.InvalidIssuerCredentialInfo,
            CredentialError.UnsupportedKeyStorageSecurityLevel,
            CredentialError.UnsupportedCryptographicSuite,
            is CredentialError.InvalidSignedMetadata,
            CredentialError.NetworkError,
            CredentialError.UnsupportedImageFormat -> {
                Timber.d(message = "Deferred refresh: fetch deferred failed with error $error")
            }

            is CredentialError.Unexpected -> {
                Timber.w(t = error.cause, message = "Deferred refresh: fetch deferred failed")
            }
        }
    }

    private suspend fun invalidateCredential(deferredCredentialEntity: DeferredCredentialWithAuthenticationAndKeyBinding) =
        deferredCredentialRepository.updateStatus(
            credentialId = deferredCredentialEntity.credential.id,
            progressionState = DeferredProgressionState.INVALID,
            polledAt = Instant.now().epochSecond,
            pollInterval = deferredCredentialEntity.deferredCredential.pollInterval,
        )

    private suspend fun handleCredentialError(
        deferredCredentialEntity: DeferredCredentialWithAuthenticationAndKeyBinding,
        error: SaveCredentialFromDeferredError,
    ) = when (error) {
        CredentialError.InvalidIssuerCredentialInfo,
        CredentialError.IntegrityCheckFailed,
        CredentialError.InvalidCredentialOffer,
        CredentialError.InvalidGenerateMetadataClaims,
        CredentialError.InvalidJsonScheme,
        CredentialError.UnknownIssuer,
        CredentialError.UnverifiedIssuer,
        CredentialError.UnauthorizedIssuance,
        CredentialError.UnknownRegistry,
        CredentialError.UnsupportedCredentialFormat,
        CredentialError.UnsupportedKeyStorageSecurityLevel,
        CredentialError.IncompatibleDeviceKeyStorage -> failedCredential(deferredCredentialEntity)

        CredentialError.NetworkError,
        CredentialError.UnsupportedImageFormat -> {
            Timber.d(message = "Deferred refresh: fetch deferred failed with error $error")
        }

        is CredentialError.Unexpected -> {
            Timber.w(t = error.cause, message = "Deferred refresh: fetch credential failed")
        }
    }

    private suspend fun failedCredential(deferredCredentialEntity: DeferredCredentialWithAuthenticationAndKeyBinding) =
        deferredCredentialRepository.updateStatus(
            credentialId = deferredCredentialEntity.credential.id,
            progressionState = DeferredProgressionState.FAILED,
            polledAt = Instant.now().epochSecond,
            pollInterval = deferredCredentialEntity.deferredCredential.pollInterval,
        )
}
