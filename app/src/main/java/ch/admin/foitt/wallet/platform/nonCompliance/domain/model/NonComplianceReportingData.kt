package ch.admin.foitt.wallet.platform.nonCompliance.domain.model

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetadataDisplayData
import kotlinx.serialization.Serializable

@Serializable
data class NonComplianceReportingData(
    val actorDisplayData: ActorMetadataDisplayData,
    val rawData: String?,
    val issuerDid: String?,
    val activityType: ActivityType,
)
