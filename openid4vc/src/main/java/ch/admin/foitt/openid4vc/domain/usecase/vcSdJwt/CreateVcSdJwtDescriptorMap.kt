package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.DescriptorMap
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor

internal fun interface CreateVcSdJwtDescriptorMap {
    suspend operator fun invoke(
        inputDescriptor: InputDescriptor,
        credentialIndex: Int,
    ): DescriptorMap
}
