package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchCredentialError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadata
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadataRepositoryError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.openid4vc.domain.repository.TypeMetadataRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import ch.admin.foitt.openid4vc.utils.SafeJson
import ch.admin.foitt.sriValidator.domain.SRIValidator
import ch.admin.foitt.sriValidator.domain.model.SRIError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import timber.log.Timber
import java.net.URL
import javax.inject.Inject

internal class FetchVcSdJwtCredentialImpl @Inject constructor(
    private val fetchVerifiableCredential: FetchVerifiableCredential,
    private val verifyJwtSignature: VerifyJwtSignature,
    private val typeMetadataRepository: TypeMetadataRepository,
    private val safeJson: SafeJson,
    private val sriValidator: SRIValidator,
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

        val vctUrl = getVctUrl(credential.vct)
        if (vctUrl.isOk) {
            val typeMetadata = fetchTypeMetadata(vctUrl.value, credential.vct, credential.vctIntegrity).bind()
        }

        credential
    }

    private fun getVctUrl(vct: String): Result<URL, Unit> = runSuspendCatching {
        URL(vct)
    }.mapError { throwable ->
        Timber.w(t = throwable, message = "Vct is not a url")
    }

    private suspend fun fetchTypeMetadata(
        vctURL: URL,
        credentialVct: String,
        credentialVctIntegrity: String?,
    ): Result<TypeMetadata, FetchCredentialError> = coroutineBinding {
        val typeMetadataString = typeMetadataRepository.fetchTypeMetadata(vctURL)
            .mapError(TypeMetadataRepositoryError::toFetchCredentialError)
            .bind()

        // according to https://www.ietf.org/archive/id/draft-ietf-oauth-sd-jwt-vc-05.html#name-type-metadata sections 6, 8 and 9

        val typeMetadata = safeJson.safeDecodeStringTo<TypeMetadata>(typeMetadataString)
            .mapError(JsonParsingError::toFetchCredentialError)
            .bind()

        if (typeMetadata.vct != credentialVct) {
            Err(CredentialOfferError.InvalidCredentialOffer).bind<FetchCredentialError>()
        }

        if (credentialVctIntegrity == null) {
            Err(CredentialOfferError.InvalidCredentialOffer).bind<FetchCredentialError>()
        } else {
            validateSubresource(credentialVctIntegrity, typeMetadataString).bind()
        }

        typeMetadata
    }

    private fun validateSubresource(
        vctIntegrity: String,
        typeMetadataString: String,
    ): Result<Unit, FetchCredentialError> = runSuspendCatching {
        sriValidator.validate(typeMetadataString.encodeToByteArray(), vctIntegrity)
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "SRI validation error")
        when (throwable) {
            is SRIError.MalformedIntegrity,
            is SRIError.UnsupportedAlgorithm -> CredentialOfferError.InvalidCredentialOffer
            else -> CredentialOfferError.Unexpected(throwable)
        }
    }.andThen { isValid ->
        if (isValid) {
            Ok(Unit)
        } else {
            Err(CredentialOfferError.InvalidCredentialOffer)
        }
    }
}
