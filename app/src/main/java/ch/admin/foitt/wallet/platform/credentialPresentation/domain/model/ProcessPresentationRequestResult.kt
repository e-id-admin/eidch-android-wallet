package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest

sealed interface ProcessPresentationRequestResult {
    data class Credential(
        val credential: CompatibleCredential,
        val presentationRequest: PresentationRequest,
        val shouldFetchTrustStatements: Boolean,
    ) : ProcessPresentationRequestResult

    data class CredentialList(
        val credentials: List<CompatibleCredential>,
        val presentationRequest: PresentationRequest,
        val shouldFetchTrustStatements: Boolean,
    ) : ProcessPresentationRequestResult
}
