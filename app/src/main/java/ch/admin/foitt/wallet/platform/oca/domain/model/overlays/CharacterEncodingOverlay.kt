package ch.admin.foitt.wallet.platform.oca.domain.model.overlays

import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface CharacterEncodingOverlay : Overlay

@Serializable
data class CharacterEncodingOverlay1x0(
    @SerialName("capture_base")
    override val captureBaseDigest: String,

    @SerialName("default_character_encoding")
    val defaultCharacterEncoding: String? = null,
    @SerialName("attribute_character_encoding")
    val attributeCharacterEncoding: Map<String, String> = emptyMap()
) : CharacterEncodingOverlay {
    override val type: OverlaySpecType = OverlaySpecType.CHARACTER_ENCODING_1_0
}
