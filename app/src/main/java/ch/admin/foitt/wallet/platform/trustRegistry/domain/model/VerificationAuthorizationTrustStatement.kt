package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerificationAuthorizationTrustStatement(
    // mandatory for all v2 trust statements
    override val typ: String,
    override val alg: SignatureAlgorithm,
    override val kid: String,
    override val profileVersion: String,
    override val jti: String,
    override val iat: Long,
    override val exp: Long,
    // verification authorization TS also contains
    val sub: String,
    val status: CredentialStatusProperties,
    @SerialName(CLAIM_NAME_AUTHORIZED_FIELDS)
    val authorizedFields: Set<String>,
) : TrustStatementV2 {
    companion object {
        const val TYPE = "swiyu-protected-verification-authorization-trust-statement+jwt"
        const val CLAIM_NAME_AUTHORIZED_FIELDS = "authorized_fields"
    }
}
