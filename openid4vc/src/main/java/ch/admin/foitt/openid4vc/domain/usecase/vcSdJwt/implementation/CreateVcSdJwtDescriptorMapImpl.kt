package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.DescriptorMap
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtDescriptorMap
import javax.inject.Inject

internal class CreateVcSdJwtDescriptorMapImpl @Inject constructor() : CreateVcSdJwtDescriptorMap {
    override suspend fun invoke(
        inputDescriptor: InputDescriptor,
        credentialIndex: Int,
    ) = DescriptorMap(
        format = CredentialFormat.VC_SD_JWT.format,
        id = inputDescriptor.id,
        path = "$",
    )
}
