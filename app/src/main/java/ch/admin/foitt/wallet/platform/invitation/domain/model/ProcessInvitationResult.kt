package ch.admin.foitt.wallet.platform.invitation.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest as Oid4vcPresentation

sealed interface ProcessInvitationResult {
    data class CredentialOffer(
        val credentialId: Long,
    ) : ProcessInvitationResult
    data class PresentationRequest(
        val credential: CompatibleCredential,
        val request: Oid4vcPresentation,
    ) : ProcessInvitationResult
    data class PresentationRequestCredentialList(
        val credentials: List<CompatibleCredential>,
        val request: Oid4vcPresentation
    ) : ProcessInvitationResult
}

internal fun ProcessPresentationRequestResult.toProcessInvitationResult(request: PresentationRequest): ProcessInvitationResult =
    when (this) {
        is ProcessPresentationRequestResult.Credential -> ProcessInvitationResult.PresentationRequest(credential, request)
        is ProcessPresentationRequestResult.CredentialList ->
            ProcessInvitationResult.PresentationRequestCredentialList(credentials, request)
    }
