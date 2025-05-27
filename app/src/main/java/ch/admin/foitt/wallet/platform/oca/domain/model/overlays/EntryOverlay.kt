package ch.admin.foitt.wallet.platform.oca.domain.model.overlays

import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

sealed interface EntryOverlay : LocalizedOverlay

@Serializable
data class EntryOverlay1x0(
    @SerialName("capture_base")
    override val captureBaseDigest: String,

    @SerialName("language")
    override val language: String,

    @SerialName("attribute_entries")
    val attributeEntries: JsonObject
) : EntryOverlay {
    override val type: OverlaySpecType = OverlaySpecType.ENTRY_1_0
}
