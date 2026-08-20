package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthorizationResponseResponse(
    @SerialName("redirect_uri")
    val redirectUri: String? = null,
)
