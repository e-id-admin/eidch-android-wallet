package ch.admin.foitt.wallet.platform.navArgs.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ClientNameDisplay
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.LogoUriDisplay

data class PresentationCredentialListNavArg(
    val compatibleCredentials: Array<CompatibleCredential>,
    val presentationRequest: PresentationRequest,
    val clientDisplay: Array<ClientNameDisplay>,
    val uriDisplay: Array<LogoUriDisplay>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PresentationCredentialListNavArg

        if (!compatibleCredentials.contentEquals(other.compatibleCredentials)) return false
        return presentationRequest == other.presentationRequest
    }

    override fun hashCode(): Int {
        var result = compatibleCredentials.contentHashCode()
        result = 31 * result + presentationRequest.hashCode()
        return result
    }
}
