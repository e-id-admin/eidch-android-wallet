package ch.admin.foitt.openid4vc.domain.model.anycredential

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import kotlinx.serialization.json.JsonElement

interface AnyCredential {
    val id: Long?
    val signingKeyId: String?
    val signingAlgorithm: SigningAlgorithm?
    val payload: String
    val format: CredentialFormat
    val json: JsonElement
    val claimsPath: String
    val validity: CredentialValidity

    fun createVerifiableCredential(requestedFieldKeys: List<String>): String
}
