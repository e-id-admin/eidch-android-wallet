package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.CredentialValidity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import kotlinx.serialization.json.JsonElement

class VcSdJwtCredential(
    override val id: Long? = null,
    override val keyBindingIdentifier: String?,
    override val keyBindingAlgorithm: SigningAlgorithm?,
    override val payload: String,
) : VcSdJwt(payload), AnyCredential {

    override val issuer: String = this.vcIssuer
    override val format: CredentialFormat = CredentialFormat.VC_SD_JWT

    override val validity: CredentialValidity
        get() = jwtValidity

    override val json: JsonElement = sdJwtJson

    override val claimsPath = "$"

    override fun createVerifiableCredential(requestedFieldKeys: List<String>): String = createSelectiveDisclosure(requestedFieldKeys)
}
