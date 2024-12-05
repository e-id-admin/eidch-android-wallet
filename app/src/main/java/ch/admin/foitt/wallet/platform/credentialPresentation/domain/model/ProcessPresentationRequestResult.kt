package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

sealed interface ProcessPresentationRequestResult {
    data class Credential(
        val credential: CompatibleCredential,
    ) : ProcessPresentationRequestResult

    data class CredentialList(
        val credentials: List<CompatibleCredential>,
    ) : ProcessPresentationRequestResult
}
