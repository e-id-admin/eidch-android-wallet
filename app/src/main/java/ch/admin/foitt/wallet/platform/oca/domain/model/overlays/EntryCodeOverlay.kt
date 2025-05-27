package ch.admin.foitt.wallet.platform.oca.domain.model.overlays

import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

sealed interface EntryCodeOverlay : Overlay

@Serializable
data class EntryCodeOverlay1x0(
    @SerialName("capture_base")
    override val captureBaseDigest: String,

    @SerialName("attribute_entry_codes")
    val attributeEntryCodes: JsonObject
) : EntryCodeOverlay {
    override val type: OverlaySpecType = OverlaySpecType.ENTRY_CODE_1_0
}
