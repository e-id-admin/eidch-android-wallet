package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uniffi.heidi_dcql_rust.DcqlQuery

@Serializable
data class VerificationQueryPublicStatement(
    override val typ: String,
    override val alg: SignatureAlgorithm,
    override val kid: String,
    @SerialName("profile_version")
    override val profileVersion: String,
    override val jti: String,
    override val iat: Long,
    override val exp: Long,
    @SerialName("sub")
    val sub: String,
    @SerialName("purpose_name")
    val purposeName: Map<String, String>,
    @SerialName("purpose_description")
    val purposeDescription: Map<String, String>,
    @SerialName("request")
    val request: VqPsRequest,
) : TrustStatementV2 {
    companion object {
        const val TYPE = "swiyu-verification-query-public-statement+jwt"
        const val CLAIM_NAME_PROFILE_VERSION = "profile_version"
        const val CLAIM_NAME_JTI = "jti"
        const val CLAIM_NAME_PURPOSE_NAME = "purpose_name"
        const val CLAIM_NAME_PURPOSE_DESCRIPTION = "purpose_description"
        const val CLAIM_NAME_REQUEST = "request"
    }
}

@Serializable
data class VqPsRequest(
    @SerialName("type")
    val type: String,
    @SerialName("scope")
    val scope: String?,
    @SerialName("query")
    val query: DcqlQuery,
) {
    companion object {
        const val TYPE_DCQL = "DCQL"
    }
}
