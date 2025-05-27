package ch.admin.foitt.wallet.platform.oca.domain.model.overlays

import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface MetaOverlay : LocalizedOverlay

@Serializable
data class MetaOverlay1x0(
    @SerialName("capture_base")
    override val captureBaseDigest: String,

    @SerialName("language")
    override val language: String,

    @SerialName("name")
    val name: String? = null,
    @SerialName("description")
    val description: String? = null,
) : MetaOverlay {
    override val type: OverlaySpecType = OverlaySpecType.META_1_0
}
