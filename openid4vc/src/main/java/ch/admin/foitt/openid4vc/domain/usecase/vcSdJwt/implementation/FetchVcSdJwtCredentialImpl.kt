package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchCredentialError
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
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
            signingKeyId = verifiableCredential.signingKeyId,
            signingAlgorithm = verifiableCredential.signingAlgorithm,
            payload = verifiableCredential.credential,
        )

        val sdJwt = SdJwt(credential.payload)
        val issuerDid = runSuspendCatching {
            sdJwt.issuer
        }.mapError(Throwable::toFetchCredentialError)
            .bind()
        val signedJwt = runSuspendCatching {
            sdJwt.signedJWT
        }.mapError(Throwable::toFetchCredentialError)
            .bind()

        verifyJwtSignature(
            issuerDid = issuerDid,
            signedJwt = signedJwt,
        ).mapError(VerifyJwtError::toFetchCredentialError)
            .bind()

        credential
    }
}
