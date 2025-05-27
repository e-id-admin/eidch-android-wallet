package ch.admin.foitt.wallet.platform.oca.domain.model.overlays

import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ClusterOrderingOverlay : LocalizedOverlay

@Serializable
data class ClusterOrderingOverlay1x0(
    @SerialName("capture_base")
    override val captureBaseDigest: String,

    @SerialName("language")
    override val language: String,

    @SerialName("cluster_order")
    val clusterOrder: Map<String, Int>,
    @SerialName("attribute_cluster_order")
    val attributeClusterOrder: Map<String, Map<String, Int>>,
    @SerialName("cluster_labels")
    val clusterLabels: Map<String, String> = emptyMap()
) : ClusterOrderingOverlay {
    override val type: OverlaySpecType = OverlaySpecType.CLUSTER_ORDERING_1_0
}
