package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class CredentialRequestProof

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CredentialRequestProofJwt(
    @SerialName("jwt")
    val jwt: String
) : CredentialRequestProof() {
    @EncodeDefault
    @SerialName("proof_type")
    val proofType: String = TYPE

    companion object {
        const val TYPE = "jwt"
    }
}
