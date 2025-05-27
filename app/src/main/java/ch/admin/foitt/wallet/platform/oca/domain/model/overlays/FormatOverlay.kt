package ch.admin.foitt.wallet.platform.oca.domain.model.overlays

import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface FormatOverlay : Overlay

@Serializable
data class FormatOverlay1x0(
    @SerialName("capture_base")
    override val captureBaseDigest: String,

    @SerialName("attribute_formats")
    val attributeFormats: Map<String, String>,
) : FormatOverlay {
    override val type: OverlaySpecType = OverlaySpecType.FORMAT_1_0
}
