package ch.admin.foitt.openid4vc.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HttpErrorBody(
    @SerialName("error")
    val error: String,
    @SerialName("error_code")
    val errorCode: String? = null,
    @SerialName("error_description")
    val errorDescription: String? = null,
)
