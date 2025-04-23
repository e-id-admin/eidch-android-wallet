package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchCredentialError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class FetchVcSdJwtCredentialImpl @Inject constructor(
    private val fetchVerifiableCredential: FetchVerifiableCredential,
    private val verifyJwtSignature: VerifyJwtSignature,
) : FetchVcSdJwtCredential {
    override suspend fun invoke(
        credentialConfig: VcSdJwtCredentialConfiguration,
        credentialOffer: CredentialOffer,
    ): Result<VcSdJwtCredential, FetchCredentialError> = coroutineBinding {
        val verifiableCredential = fetchVerifiableCredential(
            credentialConfiguration = credentialConfig,
            credentialOffer = credentialOffer,
        ).mapError(FetchVerifiableCredentialError::toFetchCredentialError)
            .bind()

        val credential = VcSdJwtCredential(
            keyBindingIdentifier = verifiableCredential.keyBindingIdentifier,
            keyBindingAlgorithm = verifiableCredential.keyBindingAlgorithm,
            payload = verifiableCredential.credential,
        )

        if (credential.hasNonDisclosableClaims()) {
            Err(CredentialOfferError.InvalidCredentialOffer).bind<FetchCredentialError>()
        }

        val issuerDid = runSuspendCatching {
            credential.issuer
        }.mapError { throwable ->
            throwable.toFetchCredentialError("FetchVcSdJwtCredential credential issuer error")
        }.bind()
        val keyId = runSuspendCatching {
            credential.kid
        }.mapError { throwable ->
            throwable.toFetchCredentialError("FetchVcSdJwtCredential credential kid error")
        }.bind()

        verifyJwtSignature(
            did = issuerDid,
            kid = keyId,
            jwt = credential,
        ).mapError(VerifyJwtError::toFetchCredentialError)
            .bind()

        credential
    }
}
