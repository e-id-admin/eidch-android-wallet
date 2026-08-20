package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProtectedIssuanceAuthorizationTrustStatement(
    // mandatory for all v2 trust statements
    override val typ: String,
    override val alg: SignatureAlgorithm,
    override val kid: String,
    override val profileVersion: String,
    override val jti: String,
    override val iat: Long,
    override val exp: Long,
    // piaTS also contains
    val sub: String,
    val status: CredentialStatusProperties,
    @SerialName("can_issue")
    val protectedIssuanceAuthorizationObject: ProtectedIssuanceAuthorizationObject,
) : TrustStatementV2 {
    companion object {
        const val TYPE = "swiyu-protected-issuance-authorization-trust-statement+jwt"
        const val CLAIM_NAME_CAN_ISSUE = "can_issue"
    }
}

@Serializable(with = ProtectedIssuanceAuthorizationObjectSerializer::class)
data class ProtectedIssuanceAuthorizationObject(
    val vct: String,
    val vctName: String,
    val reason: Map<String, String>,
)
