package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateDidJwkError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialRequestProofJwt
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerConfigurationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInformationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.Grant
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequestProofJwt
import ch.admin.foitt.openid4vc.domain.usecase.CreateDidJwk
import ch.admin.foitt.openid4vc.domain.usecase.DeleteKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.GenerateKeyPair
import ch.admin.foitt.openid4vc.utils.retryUseCase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import javax.inject.Inject

internal class FetchVerifiableCredentialImpl @Inject constructor(
    private val credentialOfferRepository: CredentialOfferRepository,
    private val generateKeyPair: GenerateKeyPair,
    private val createDidJwk: CreateDidJwk,
    private val createCredentialRequestProofJwt: CreateCredentialRequestProofJwt,
    private val deleteKeyPair: DeleteKeyPair,
) : FetchVerifiableCredential {
    override suspend fun invoke(
        credentialConfiguration: AnyCredentialConfiguration,
        credentialOffer: CredentialOffer,
    ) = coroutineBinding {
        val issuerEndpoint = credentialOffer.credentialIssuer
        if (credentialConfiguration.identifier !in credentialOffer.credentialConfigurationIds) {
            return@coroutineBinding Err(CredentialOfferError.InvalidCredentialOffer).bind<VerifiableCredential>()
        }

        credentialConfiguration.proofTypesSupported.let { proofTypes ->
            if (proofTypes.isNotEmpty() && proofTypes.keys.none { it == ProofType.JWT }) {
                return@coroutineBinding Err(CredentialOfferError.UnsupportedProofType).bind<VerifiableCredential>()
            }
        }
        credentialConfiguration.cryptographicBindingMethodsSupported?.let {
            if (it.isNotEmpty() && it.intersect(listOf("did:jwk", "did:tdw")).isEmpty()) {
                return@coroutineBinding Err(CredentialOfferError.UnsupportedCryptographicSuite).bind<VerifiableCredential>()
            }
        }

        val issuerConfig = credentialOfferRepository.fetchIssuerConfiguration(issuerEndpoint)
            .mapError(FetchIssuerConfigurationError::toFetchVerifiableCredentialError)
            .bind()

        val issuerInfo = credentialOfferRepository.fetchIssuerCredentialInformation(issuerEndpoint)
            .mapError(FetchIssuerCredentialInformationError::toFetchVerifiableCredentialError)
            .bind()

        val keyPair =
            if (credentialConfiguration.proofTypesSupported.isNotEmpty()) {
                generateKeyPair(credentialConfiguration).bind()
            } else {
                null
            }

        val tokenResponse = getToken(issuerConfig.tokenEndpoint, credentialOffer.grants)
            .onFailure { keyPair?.let { deleteKeyPair(keyPair.keyId) } }
            .bind()

        var credentialRequestJwt: CredentialRequestProofJwt? = null

        if (keyPair != null) {
            val jwk = createDidJwk(keyPair = keyPair.keyPair, algorithm = keyPair.algorithm, asDid = false)
                .mapError(CreateDidJwkError::toFetchVerifiableCredentialError)
                .onFailure { deleteKeyPair(keyPair.keyId) }
                .bind()

            credentialRequestJwt = retryUseCase {
                createCredentialRequestProofJwt(
                    keyPair = keyPair,
                    jwk = jwk,
                    issuer = issuerEndpoint,
                    cNonce = tokenResponse.cNonce
                )
            }.onFailure { deleteKeyPair(keyPair.keyId) }
                .bind()
        }

        val credentialResponse = credentialOfferRepository.fetchCredential(
            issuerInfo.credentialEndpoint,
            credentialConfiguration,
            credentialRequestJwt,
            tokenResponse.accessToken
        )
            .onFailure { keyPair?.let { deleteKeyPair(keyPair.keyId) } }
            .bind()

        VerifiableCredential(
            credential = credentialResponse.credential,
            format = credentialConfiguration.format,
            signingKeyId = keyPair?.keyId,
            signingAlgorithm = keyPair?.algorithm,
        )
    }

    private suspend fun getToken(tokenEndpoint: String, grant: Grant) =
        if (grant.preAuthorizedCode != null) {
            credentialOfferRepository.fetchAccessToken(
                tokenEndpoint,
                grant.preAuthorizedCode.preAuthorizedCode
            )
        } else {
            Err(CredentialOfferError.UnsupportedGrantType)
        }
}
