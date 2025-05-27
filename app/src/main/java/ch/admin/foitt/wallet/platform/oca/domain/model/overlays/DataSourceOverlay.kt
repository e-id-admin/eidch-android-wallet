package ch.admin.foitt.wallet.platform.oca.domain.model.overlays

import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface DataSourceOverlay : Overlay

/**
 * Data Source Mapping Overlay v1.0
 * https://github.com/e-id-admin/open-source-community/blob/main/tech-roadmap/rfcs/oca/spec.md#data-source-mapping-overlay
 *
 * @property format The format for which the data source mapping can be used to.
 * @property attributeSources A mapping of attributes to their JSONPath data sources.
 */
@Serializable
data class DataSourceOverlay1x0(
    @SerialName("capture_base")
    override val captureBaseDigest: String,
    @SerialName("format")
    val format: String,
    @SerialName("attribute_sources")
    val attributeSources: Map<String, String>
) : DataSourceOverlay {
    @SerialName("type")
    override val type: OverlaySpecType = OverlaySpecType.DATA_SOURCE_1_0
}
