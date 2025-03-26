package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrustStatement(
    @SerialName("exp")
    val exp: Long,
    @SerialName("iat")
    val iat: Long,
    @SerialName("iss")
    val iss: String,
    @SerialName("logoUri")
    val logoUri: Map<String, String>?,
    @SerialName("nbf")
    val nbf: Long,
    @SerialName("orgName")
    val orgName: Map<String, String>,
    @SerialName("prefLang")
    val prefLang: String,
    @SerialName("_sd_alg")
    val sdAlg: String,
    @SerialName("status")
    val status: TrustStatementStatus?,
    @SerialName("sub")
    val sub: String,
    @SerialName("vct")
    val vct: String
)

@Serializable
data class TrustStatementStatus(
    @SerialName("status_list")
    val statusList: TrustStatementStatusList
)

@Serializable
data class TrustStatementStatusList(
    @SerialName("idx")
    val idx: Int,
    @SerialName("uri")
    val uri: String
)
