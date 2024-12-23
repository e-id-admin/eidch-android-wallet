package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData

fun interface FetchVerifierDisplayData {
    suspend operator fun invoke(presentationRequest: PresentationRequest): ActorDisplayData
}
