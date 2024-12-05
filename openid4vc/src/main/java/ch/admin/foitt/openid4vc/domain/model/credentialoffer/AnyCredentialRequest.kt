package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.AnyCredentialRequest.CredentialDefinition
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.UnknownCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

open class AnyCredentialRequest {
    @Serializable
    data class CredentialDefinition(
        @SerialName("types")
        val type: List<String>
    )
}

fun AnyCredentialConfiguration.toCredentialRequest(
    credentialId: String,
    proof: CredentialRequestProof?,
): AnyCredentialRequest = when (this) {
    is VcSdJwtCredentialConfiguration -> VcSdJwtCredentialRequest(
        credentialDefinition = CredentialDefinition(listOf(credentialId)),
        format = this.format,
        vct = this.vct,
        proof = proof,
    )
    is UnknownCredentialConfiguration -> error("unsupported credential configuration")
}
