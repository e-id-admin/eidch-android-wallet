package ch.admin.foitt.openid4vc.domain.model.keyBinding

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Jwk(
    @SerialName("x")
    val x: String,
    @SerialName("y")
    val y: String,
    @SerialName("crv")
    val crv: String,
    @SerialName("kty")
    val kty: String,
    @SerialName("kid")
    val kid: String? = null,
)
