package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.CreateJWSKeyPairError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toCredentialOfferError
import ch.admin.foitt.openid4vc.domain.usecase.CreateJWSKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GenerateKeyPair
import ch.admin.foitt.openid4vc.utils.Constants.ANDROID_KEY_STORE
import ch.admin.foitt.openid4vc.utils.retryUseCase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class GenerateKeyPairImpl @Inject constructor(
    private val createJWSKeyPair: CreateJWSKeyPair,
) : GenerateKeyPair {

    override suspend fun invoke(
        credentialConfiguration: AnyCredentialConfiguration
    ): Result<JWSKeyPair, FetchVerifiableCredentialError> = coroutineBinding {
        val cryptographicSuite = getCryptographicSuite(credentialConfiguration).bind()
        val keyPair = createKeyPair(cryptographicSuite).bind()

        keyPair
    }

    private fun getCryptographicSuite(
        credentialConfiguration: AnyCredentialConfiguration
    ): Result<SigningAlgorithm, CredentialOfferError.UnsupportedCryptographicSuite> {
        val supportedProofType = credentialConfiguration.proofTypesSupported.entries.firstOrNull {
            it.key != ProofType.UNKNOWN
        }?.value
        val cryptographicSuite = supportedProofType?.signingAlgorithms?.firstOrNull()
        return when (cryptographicSuite) {
            null -> Err(CredentialOfferError.UnsupportedCryptographicSuite)
            else -> Ok(cryptographicSuite)
        }
    }

    private suspend fun createKeyPair(
        signingAlgorithm: SigningAlgorithm
    ): Result<JWSKeyPair, FetchVerifiableCredentialError> = when (signingAlgorithm) {
        SigningAlgorithm.ES512, SigningAlgorithm.ES256 -> {
            retryUseCase {
                createJWSKeyPair(signingAlgorithm, ANDROID_KEY_STORE)
            }.mapError(CreateJWSKeyPairError::toCredentialOfferError)
        }
    }
}
