package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import kotlinx.serialization.Serializable

@Serializable
data class PresentationFlowContext(
    val proximityOrigin: String? = null,
    val presentationPaths: List<ClaimsPathPointer> = emptyList(),
    val dcqlQueryId: String? = null,
)
