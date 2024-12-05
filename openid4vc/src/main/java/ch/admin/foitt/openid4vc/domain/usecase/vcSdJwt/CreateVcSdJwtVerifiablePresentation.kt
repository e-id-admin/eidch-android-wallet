package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.CreateVcSdJwtVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import com.github.michaelbull.result.Result

internal fun interface CreateVcSdJwtVerifiablePresentation {
    suspend operator fun invoke(
        credential: VcSdJwtCredential,
        requestedFields: List<String>,
        presentationRequest: PresentationRequest,
    ): Result<String, CreateVcSdJwtVerifiablePresentationError>

    companion object {
        const val VP_KEY = "vp"
    }
}
