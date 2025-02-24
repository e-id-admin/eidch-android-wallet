package ch.admin.foitt.openid4vc.domain.model.anycredential

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import kotlinx.serialization.json.JsonElement

interface AnyCredential {
    val id: Long?
    val keyBindingIdentifier: String?
    val keyBindingAlgorithm: SigningAlgorithm?
    val payload: String
    val format: CredentialFormat
    val json: JsonElement
    val claimsPath: String
    val validity: CredentialValidity
    val issuer: String?

    fun createVerifiableCredential(requestedFieldKeys: List<String>): String
}
