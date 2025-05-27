package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundlerError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaCaptureBaseValidationError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaCesrHashValidatorError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaOverlayValidationError
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.UnsupportedOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.toOcaBundlerError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCaptureBaseValidator
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCesrHashValidator
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaOverlayValidator
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import javax.inject.Inject

class OcaBundlerImpl @Inject constructor(
    private val json: SafeJson,
    private val ocaCesrHashValidator: OcaCesrHashValidator,
    private val ocaCaptureBaseValidator: OcaCaptureBaseValidator,
    private val ocaOverlayValidator: OcaOverlayValidator,
) : OcaBundler {

    override suspend fun invoke(jsonString: String): Result<OcaBundle, OcaBundlerError> = coroutineBinding {
        val ocaBundleJsonObject = json.safeDecodeStringTo<JsonObject>(jsonString)
            .mapError(JsonParsingError::toOcaBundlerError)
            .bind()

        val captureBasesJson = ocaBundleJsonObject["capture_bases"]?.jsonArray
            ?: return@coroutineBinding Err(OcaError.InvalidJsonObject).bind<OcaBundle>()

        captureBasesJson.forEach { captureBaseArrayElement ->
            ocaCesrHashValidator(captureBaseArrayElement.toString())
                .mapError(OcaCesrHashValidatorError::toOcaBundlerError)
                .bind()
        }

        val ocaBundle = json.safeDecodeStringTo<OcaBundle>(jsonString)
            .mapError(JsonParsingError::toOcaBundlerError)
            .bind()

        val bundleWithSupportedOverlays = ocaBundle.copy(overlays = ocaBundle.overlays.filterNot { it is UnsupportedOverlay })

        val validCaptureBases = ocaCaptureBaseValidator(bundleWithSupportedOverlays.captureBases)
            .mapError(OcaCaptureBaseValidationError::toOcaBundlerError)
            .bind()

        val validOverlays = ocaOverlayValidator(bundleWithSupportedOverlays)
            .mapError(OcaOverlayValidationError::toOcaBundlerError)
            .bind()

        OcaBundle(
            captureBases = validCaptureBases,
            overlays = validOverlays
        )
    }
}
