package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaOverlayValidationError
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LocalizedOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaOverlayValidator
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import javax.inject.Inject

class OcaOverlayValidatorImpl @Inject constructor() : OcaOverlayValidator {
    override suspend fun invoke(
        ocaBundle: OcaBundle,
    ): Result<List<Overlay>, OcaOverlayValidationError> = coroutineBinding {
        val captureBases = ocaBundle.captureBases
        val overlays = ocaBundle.overlays

        if (overlays.containInvalidReferences(captureBases)) {
            return@coroutineBinding Err(OcaError.InvalidOverlayCaptureBaseDigest).bind<List<Overlay>>()
        }

        if (overlays.containInvalidLanguageCodes()) {
            return@coroutineBinding Err(OcaError.InvalidOverlayLanguageCode).bind<List<Overlay>>()
        }

        if (overlays.filterIsInstance<DataSourceOverlay>().isInvalid()) {
            return@coroutineBinding Err(OcaError.InvalidDataSourceOverlay).bind<List<Overlay>>()
        }

        overlays
    }

    private fun List<Overlay>.containInvalidReferences(captureBases: List<CaptureBase>): Boolean {
        return this.any { overlay ->
            captureBases.none { it.digest == overlay.captureBaseDigest }
        }
    }

    private fun List<Overlay>.containInvalidLanguageCodes(): Boolean {
        return this.filterIsInstance<LocalizedOverlay>().any { overlay ->
            languageRegex.matches(overlay.language).not()
        }
    }

    private fun List<DataSourceOverlay>.isInvalid(): Boolean {
        val jsonPaths = this.flatMap { dataSourceOverlay ->
            when (dataSourceOverlay) {
                is DataSourceOverlay1x0 -> dataSourceOverlay.attributeSources.values
            }
        }

        val containsInvalidJsonPath = jsonPaths.any { path ->
            validJsonPathRegex.matches(path).not()
        }

        return containsInvalidJsonPath
    }

    private companion object {
        val languageRegex = Regex("^[a-z]{2}(-[A-Z]{2})?$")

        // Matches a root identifier `$` followed by one or more children in dot or bracket notation, e.g. e.g. `$.a`, `$["a"]`, `$['a']`
        // Arrays with a non-negative integer index or wildcards are accepted, e.g. `$.x[0]` or `$.x[*]`
        // See https://github.com/e-id-admin/open-source-community/blob/main/tech-roadmap/rfcs/oca/spec.md#jsonpath-consideration for
        // specification and tests for examples of valid and invalid json paths
        val validJsonPathRegex = """(?x)
            ^\$(
              (
                (?<dot>\.([a-zA-Z_]+\w*)|\.\*) |
                (?<bracket>\[(\*|(?<quote>["'])([a-zA-Z_]+\w*)\k<quote>)])
              )*
              (?<array>\[\d+])*
            )+$
        """.trimMargin().toRegex()
    }
}
