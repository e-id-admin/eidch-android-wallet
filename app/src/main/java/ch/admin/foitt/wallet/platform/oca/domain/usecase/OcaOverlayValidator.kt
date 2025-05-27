package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaOverlayValidationError
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay
import com.github.michaelbull.result.Result

interface OcaOverlayValidator {
    suspend operator fun invoke(ocaBundle: OcaBundle): Result<List<Overlay>, OcaOverlayValidationError>
}
