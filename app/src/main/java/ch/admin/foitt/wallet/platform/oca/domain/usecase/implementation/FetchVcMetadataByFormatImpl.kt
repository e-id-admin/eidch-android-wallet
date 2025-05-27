package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.FetchTypeMetadataError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.FetchVcSchemaError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.Rendering
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchema
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchTypeMetadata
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSchema
import ch.admin.foitt.openid4vc.utils.SafeGetUrlError
import ch.admin.foitt.openid4vc.utils.safeGetUrl
import ch.admin.foitt.wallet.platform.jsonSchema.domain.model.JsonSchemaError
import ch.admin.foitt.wallet.platform.jsonSchema.domain.usecase.JsonSchemaValidator
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchOcaBundleError
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.model.RawOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.VcMetadata
import ch.admin.foitt.wallet.platform.oca.domain.model.toFetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchVcMetadataByFormat
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject
import javax.inject.Named

class FetchVcMetadataByFormatImpl @Inject constructor(
    private val fetchTypeMetadata: FetchTypeMetadata,
    private val fetchVcSchema: FetchVcSchema,
    private val fetchOcaBundle: FetchOcaBundle,
    @Named("VcSdJwtJsonSchemaValidator")
    private val vcSdJwtJsonSchemaValidator: JsonSchemaValidator,
) : FetchVcMetadataByFormat {
    override suspend fun invoke(
        anyCredential: AnyCredential
    ): Result<VcMetadata, FetchVcMetadataByFormatError> = coroutineBinding {
        when (anyCredential.format) {
            CredentialFormat.VC_SD_JWT -> fetchVcMetadataForVcSdJwt(anyCredential as VcSdJwtCredential).bind()
            else -> error("invalid format")
        }
    }

    private suspend fun fetchVcMetadataForVcSdJwt(
        credential: VcSdJwtCredential,
    ): Result<VcMetadata, FetchVcMetadataByFormatError> = coroutineBinding {
        var vcSchema: VcSchema? = null
        var rawOcaBundle: RawOcaBundle? = null

        val vctUrl = safeGetUrl(credential.vct)

        // fetch type metadata only if vct is a valid url
        if (vctUrl.isOk) {
            val typeMetadata = fetchTypeMetadata(vctUrl = vctUrl.value, vctIntegrity = credential.vctIntegrity)
                .mapError(FetchTypeMetadataError::toFetchVcMetadataByFormatError)
                .bind()

            // ignore vc schema if not provided in type metadata, fetch for valid url, error for invalid url
            typeMetadata.schemaUrl?.let {
                val schemaUri = safeGetUrl(typeMetadata.schemaUrl)
                    .mapError(SafeGetUrlError::toFetchVcMetadataByFormatError)
                    .bind()

                vcSchema = fetchVcSchema(
                    schemaUrl = schemaUri,
                    schemaUriIntegrity = typeMetadata.schemaUrlIntegrity,
                ).mapError(FetchVcSchemaError::toFetchVcMetadataByFormatError)
                    .bind()
            }

            // validate vcSchema
            vcSchema?.let {
                vcSdJwtJsonSchemaValidator(credential.getClaimsForPresentation().toString(), vcSchema.schema)
                    .mapError(JsonSchemaError::toFetchVcMetadataByFormatError)
                    .bind()
            }

            // find first display that contains a valid oca rendering
            val vcSdJwtOcaRendering = typeMetadata.displays
                ?.flatMap { it.renderings ?: emptyList() }
                ?.firstOrNull { it is Rendering.VcSdJwtOcaRendering } as? Rendering.VcSdJwtOcaRendering

            vcSdJwtOcaRendering?.uri?.let {
                rawOcaBundle = fetchOcaBundle(uri = it, integrity = vcSdJwtOcaRendering.uriIntegrity)
                    .mapError(FetchOcaBundleError::toFetchVcMetadataByFormatError)
                    .bind()
            }
        }

        VcMetadata(
            vcSchema = vcSchema,
            rawOcaBundle = rawOcaBundle,
        )
    }
}
