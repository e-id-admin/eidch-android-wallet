package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class IssuerConfiguration(
    @SerialName("issuer")
    val issuer: String,
    @SerialName("token_endpoint")
    val tokenEndpoint: String,
)
