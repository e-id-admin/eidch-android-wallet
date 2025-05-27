package ch.admin.foitt.wallet.platform.oca.domain.model

import kotlinx.serialization.Serializable

/**
 * Overlay Bundle Attribute, representing a Capture Base attribute with supplementary information from Overlays.
 */
@Serializable
data class OverlayBundleAttribute(
    // capture base
    val captureBaseDigest: String,
    val name: String,
    val attributeType: AttributeType,
    val flagged: Boolean = false,
    // overlays
    val labels: Map<String, String> = emptyMap(), // <language, label>
    val dataSources: Map<DataSourceFormat, JsonPath> = emptyMap(),
    val format: String? = null,
    val standard: String? = null,
    val characterEncoding: String? = null, // <language, encoding>
    val entries: Map<String, Map<String, String>> = emptyMap(), // <language, <entry-code, entry>>
    val entryCodes: List<String> = emptyList()
)
