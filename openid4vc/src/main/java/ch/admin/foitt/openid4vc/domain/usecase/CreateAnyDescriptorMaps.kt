package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.DescriptorMap
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest

internal fun interface CreateAnyDescriptorMaps {
    suspend operator fun invoke(presentationRequest: PresentationRequest): List<DescriptorMap>
}
