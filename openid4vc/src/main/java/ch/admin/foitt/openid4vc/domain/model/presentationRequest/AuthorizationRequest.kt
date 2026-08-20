package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.jwk.Jwks
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uniffi.heidi_dcql_rust.DcqlQuery

@Serializable
data class AuthorizationRequest(
    @SerialName("client_id")
    @Serializable(with = ClientIdentifierSerializer::class)
    val clientIdentifier: ClientIdentifier,
    @SerialName("response_type")
    val responseType: String,
    @SerialName("response_mode")
    val responseMode: String,
    @SerialName("response_uri")
    val responseUri: String?,
    @SerialName("nonce")
    val nonce: String,
    @SerialName("dcql_query")
    val dcqlQuery: DcqlQuery?,
    @SerialName("client_metadata")
    val clientMetaData: ClientMetaData?,
    @SerialName("state")
    val state: String?,
    @SerialName("transaction_data")
    val transactionData: String? = null,
    @SerialName("expected_origins")
    val expectedOrigins: List<String>?,
    @Serializable(with = VerifiableInfoSerializer::class)
    @SerialName("verifier_info")
    val verifierInfo: List<VerifierInfo>?,
    @SerialName("scope")
    val scope: String?,
)

@Serializable(with = ClientMetaDataSerializer::class)
data class ClientMetaData(
    val clientNameList: List<ClientName>,
    val logoUriList: List<LogoUri>,
    val jwks: Jwks? = null,
    val encryptedResponseEncValuesSupported: List<String>? = null,
)

data class ClientName(
    val clientName: String,
    val locale: String,
)

data class LogoUri(
    val logoUri: String,
    val locale: String,
)

@Serializable
data class VerifierInfo(
    @SerialName("format")
    val format: String,
    @Serializable(with = JwtSerializer::class)
    @SerialName("data")
    val data: Jwt
)

fun AuthorizationRequest.getVerificationTrustStatementJwt(trustStatementType: String) = verifierInfo?.firstOrNull { verifierInfo ->
    verifierInfo.data.type == trustStatementType
}?.data
