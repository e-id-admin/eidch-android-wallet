package ch.admin.foitt.wallet.platform.eIdApplicationProcess.presentation.mock

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StateResponseBody(
    @SerialName("state")
    val state: EIdRequestQueueState,
    @SerialName("queueInformation")
    val queueInformation: QueueInformation?,
    @SerialName("legalRepresentant")
    val legalRepresentant: LegalRepresentant?,
    @SerialName("onlineSessionStartTimeout")
    val onlineSessionStartTimeout: String,
)

@Serializable
data class QueueInformation(
    @SerialName("positionInQueue")
    val positionInQueue: Int,
    @SerialName("totalInQueue")
    val totalInQueue: Int,
    @SerialName("expectedOnlineSessionStart")
    val expectedOnlineSessionStart: String,
)

@Serializable
data class LegalRepresentant(
    @SerialName("verified")
    val verified: Boolean,
    @SerialName("verificationLink")
    val verificationLink: String,
)
