package ch.admin.foitt.wallet.platform.oca.domain.model.overlays

import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface BrandingOverlay : LocalizedOverlay

@Serializable
data class BrandingOverlay1x1(
    @SerialName("capture_base")
    override val captureBaseDigest: String,
    @SerialName("language")
    override val language: String,
    @SerialName("theme")
    val theme: String? = "light",
    @SerialName("logo")
    val logo: String? = null,
    @SerialName("background_image")
    val backgroundImage: String? = null,
    @SerialName("background_image_slice")
    val backgroundImageSlice: String? = null,
    @SerialName("primary_background_color")
    val primaryBackgroundColor: String? = null,
    @SerialName("secondary_background_color")
    val secondaryBackgroundColor: String? = null,
    @SerialName("secondary_attribute")
    val secondaryAttribute: String? = null,
    @SerialName("primary_attribute")
    val primaryAttribute: String? = null,
    @SerialName("issued_date_attribute")
    val issuedDateAttribute: String? = null,
    @SerialName("expiry_date_attribute")
    val expiryDateAttribute: String? = null,
    @SerialName("primary_field")
    val primaryField: String? = null,
    @SerialName("secondary_field")
    val secondaryField: String? = null,
) : BrandingOverlay {
    @SerialName("type")
    override val type: OverlaySpecType = OverlaySpecType.BRANDING_1_1
}
