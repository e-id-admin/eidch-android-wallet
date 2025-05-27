package ch.admin.foitt.wallet.platform.oca.domain.model.overlays

import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface StandardOverlay : Overlay

@Serializable
data class StandardOverlay1x0(
    @SerialName("capture_base")
    override val captureBaseDigest: String,

    @SerialName("attr_standards")
    val attributeStandards: Map<String, String>,
) : StandardOverlay {
    override val type: OverlaySpecType = OverlaySpecType.STANDARD_1_0
}
