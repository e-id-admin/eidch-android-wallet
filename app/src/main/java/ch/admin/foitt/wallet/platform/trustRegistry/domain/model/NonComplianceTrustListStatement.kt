package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NonComplianceTrustListStatement(
    // mandatory for all v2 trust statements
    override val typ: String,
    override val alg: SignatureAlgorithm,
    override val kid: String,
    override val profileVersion: String,
    override val jti: String,
    override val iat: Long,
    override val exp: Long,
    val status: CredentialStatusProperties,
    @SerialName("non_compliant_actors")
    val nonCompliantActors: List<NonCompliantActor>,
) : TrustStatementV2 {
    @Serializable
    data class NonCompliantActor(
        val actor: String,
        val reason: Map<String, String>? = null,
    )

    companion object {
        const val TYPE = "swiyu-non-compliance-trust-list-statement+jwt"
        const val CLAIM_NAME_NON_COMPLIANT_ACTORS = "non_compliant_actors"
        const val CLAIM_NAME_ACTOR = "actor"
        const val CLAIM_NAME_REASON = "reason"
    }
}
