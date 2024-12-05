package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep // Nimbus library uses Gson under-the-hood, which does not play well with R8
@Serializable
internal data class VerifiablePresentation(
    @SerialName("type")
    val type: List<String> = listOf("VerifiablePresentation"),
    @SerialName(VERIFIABLE_CREDENTIAL_KEY)
    val verifiableCredential: List<String>,
) {
    companion object {
        const val VERIFIABLE_CREDENTIAL_KEY = "verifiableCredential"
    }
}
