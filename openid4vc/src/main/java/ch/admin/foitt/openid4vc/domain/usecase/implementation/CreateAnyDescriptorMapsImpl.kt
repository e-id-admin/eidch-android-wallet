package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.DescriptorMap
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.usecase.CreateAnyDescriptorMaps
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtDescriptorMap
import javax.inject.Inject

internal class CreateAnyDescriptorMapsImpl @Inject constructor(
    private val createVcSdJwtDescriptorMap: CreateVcSdJwtDescriptorMap,
) : CreateAnyDescriptorMaps {
    override suspend fun invoke(presentationRequest: PresentationRequest): List<DescriptorMap> =
        presentationRequest.presentationDefinition.inputDescriptors.map { descriptor ->
            when (descriptor.formats.first()) {
                is InputDescriptorFormat.VcSdJwt -> createVcSdJwtDescriptorMap(
                    descriptor,
                    0 // we only support single credential presentation so far
                )
            }
        }
}
