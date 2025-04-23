package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.jsonSchema.domain.JsonSchema
import ch.admin.foitt.jsonSchema.domain.model.JsonSchemaError
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.Rendering
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadata
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadataRepositoryError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchema
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchemaRepositoryError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.repository.TypeMetadataRepository
import ch.admin.foitt.openid4vc.domain.repository.VcSchemaRepository
import ch.admin.foitt.openid4vc.utils.base64ToDecodedString
import ch.admin.foitt.sriValidator.domain.SRIValidator
import ch.admin.foitt.sriValidator.domain.model.SRIError
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchOcaBundleByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaRepositoryError
import ch.admin.foitt.wallet.platform.oca.domain.model.toFetchOcaBundleByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchOcaBundleByFormat
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
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

class FetchOcaBundleByFormatImpl @Inject constructor(
    private val typeMetadataRepository: TypeMetadataRepository,
    private val vcSchemaRepository: VcSchemaRepository,
    private val ocaRepository: ch.admin.foitt.wallet.platform.oca.domain.repository.OcaRepository,
    private val safeJson: SafeJson,
    private val sriValidator: SRIValidator,
    private val jsonSchema: JsonSchema,
) : FetchOcaBundleByFormat {
    override suspend fun invoke(anyCredential: AnyCredential): Result<String?, FetchOcaBundleByFormatError> = coroutineBinding {
        when (anyCredential.format) {
            CredentialFormat.VC_SD_JWT -> fetchVcSdJwtOca(anyCredential as VcSdJwtCredential).bind()
            else -> error("invalid format")
        }
    }

    private suspend fun fetchVcSdJwtOca(credential: VcSdJwtCredential): Result<String?, FetchOcaBundleByFormatError> = coroutineBinding {
        var rawOcaBundle: String? = null

        val vctUrl = safeGetUrl(credential.vct)
        if (vctUrl.isOk) {
            // first: type metadata
            val typeMetadata = fetchTypeMetadata(vctUrl.value, credential.vct, credential.vctIntegrity).bind()

            // second: vc schema
            val schemaUri = safeGetUrl(typeMetadata.schemaUrl)
            if (schemaUri.isOk) {
                val vcSchema = fetchVcSchema(
                    credentialJson = credential.getClaimsForPresentation().toString(),
                    schemaUrl = schemaUri.value,
                    schemaUriIntegrity = typeMetadata.schemaUrlIntegrity,
                ).bind()

            }

            // find first display that contains a valid oca rendering
            val vcSdJwtOcaRendering = typeMetadata.displays
                ?.flatMap { it.renderings ?: emptyList() }
                ?.firstOrNull { it is Rendering.VcSdJwtOcaRendering } as? Rendering.VcSdJwtOcaRendering

            // third: oca
            if (vcSdJwtOcaRendering != null) {
                rawOcaBundle = fetchOcaBundle(
                    uri = vcSdJwtOcaRendering.uri,
                    integrity = vcSdJwtOcaRendering.uriIntegrity
                ).bind()
            }
        }

        rawOcaBundle
    }

    private fun safeGetUrl(urlString: String?): Result<URL, FetchOcaBundleByFormatError> = runSuspendCatching {
        URL(urlString)
    }.mapError { throwable ->
        throwable.toFetchOcaBundleByFormatError(message = "string is not a valid url")
    }

    private suspend fun fetchTypeMetadata(
        vctURL: URL,
        credentialVct: String,
        credentialVctIntegrity: String?,
    ): Result<TypeMetadata, FetchOcaBundleByFormatError> = coroutineBinding {
        val typeMetadataString = typeMetadataRepository.fetchTypeMetadata(vctURL)
            .mapError(TypeMetadataRepositoryError::toFetchOcaBundleByFormatError)
            .bind()

        // according to https://www.ietf.org/archive/id/draft-ietf-oauth-sd-jwt-vc-05.html#name-type-metadata sections 6, 8 and 9

        val typeMetadata = safeJson.safeDecodeStringTo<TypeMetadata>(typeMetadataString)
            .mapError(JsonParsingError::toFetchOcaBundleByFormatError)
            .bind()

        if (typeMetadata.vct != credentialVct) {
            Err(OcaError.InvalidOca).bind<FetchOcaBundleByFormatError>()
        }

        if (credentialVctIntegrity == null) {
            Err(OcaError.InvalidOca).bind<FetchOcaBundleByFormatError>()
        } else {
            validateSubresource(typeMetadataString, credentialVctIntegrity).bind()
        }

        typeMetadata
    }

    private fun validateSubresource(
        data: String,
        integrity: String,
    ): Result<Unit, FetchOcaBundleByFormatError> = runSuspendCatching {
        sriValidator.validate(data.encodeToByteArray(), integrity)
    }.mapError { throwable ->
        Timber.e(t = throwable, message = "SRI validation error")
        when (throwable) {
            is SRIError.MalformedIntegrity,
            is SRIError.UnsupportedAlgorithm -> OcaError.InvalidOca

            else -> OcaError.Unexpected(throwable)
        }
    }.andThen { isValid ->
        if (isValid) {
            Ok(Unit)
        } else {
            Err(OcaError.InvalidOca)
        }
    }

    private suspend fun fetchVcSchema(
        credentialJson: String,
        schemaUrl: URL,
        schemaUriIntegrity: String?
    ): Result<VcSchema, FetchOcaBundleByFormatError> = coroutineBinding {
        val vcSchemaString = vcSchemaRepository.fetchVcSchema(schemaUrl)
            .mapError(VcSchemaRepositoryError::toFetchOcaBundleByFormatError)
            .bind()

        val vcSchema = VcSchema(vcSchemaString)

        schemaUriIntegrity?.let {
            validateSubresource(vcSchemaString, it).bind()
        }

        jsonSchema.validate(credentialJson.encodeToByteArray(), vcSchema.schema.encodeToByteArray())
            .mapError(JsonSchemaError::toFetchOcaBundleByFormatError)
            .bind()

        vcSchema
    }

    private suspend fun fetchOcaBundle(
        uri: String,
        integrity: String?, // uri#integrity is mandatory for https url, but optional for data uri
    ): Result<String, FetchOcaBundleByFormatError> = when {
        uri.startsWith(URL_PATTERN) && integrity != null -> fetchOcaBundleFromUrl(uri, integrity)
        uri.startsWith(DATA_URI_PATTERN) -> fetchOcaBundleFromDataUri(uri, integrity)
        else -> Err(OcaError.InvalidOca)
    }

    private suspend fun fetchOcaBundleFromUrl(
        uri: String,
        integrity: String,
    ): Result<String, FetchOcaBundleByFormatError> = coroutineBinding {
        val url = safeGetUrl(uri).bind()
        val rawOcaBundle = ocaRepository.fetchVcSdJwtOcaBundle(url)
            .mapError(OcaRepositoryError::toFetchOcaBundleByFormatError)
            .bind()

        validateSubresource(rawOcaBundle, integrity).bind()

        rawOcaBundle
    }

    private suspend fun fetchOcaBundleFromDataUri(
        uri: String,
        integrity: String?,
    ): Result<String, FetchOcaBundleByFormatError> = coroutineBinding {
        val ocaJson = uri.substringAfter(DATA_URI_PATTERN).base64ToDecodedString()

        // uri#integrity is optional for data uri
        integrity?.let {
            validateSubresource(uri, integrity).bind()
        }

        ocaJson
    }

    private companion object {
        const val URL_PATTERN = "https://"
        const val DATA_URI_PATTERN = "data:application/json;base64,"
    }
}
