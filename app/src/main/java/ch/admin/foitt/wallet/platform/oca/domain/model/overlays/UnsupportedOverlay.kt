package ch.admin.foitt.wallet.platform.oca.domain.model.overlays

import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import kotlinx.serialization.Serializable

sealed interface UnsupportedOverlay : Overlay

@Serializable
data object UnsupportedOverlayItem : UnsupportedOverlay {
    override val type: OverlaySpecType = OverlaySpecType.UNSUPPORTED
    override val captureBaseDigest: String = "unknown"
}
