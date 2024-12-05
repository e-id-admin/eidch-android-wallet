package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CredentialResponse(
    @SerialName("credential")
    val credential: String,
    @SerialName("format")
    val format: String,
    @SerialName("transaction_id")
    val transactionId: String? = null,
    @SerialName("c_nonce")
    val cNonce: String? = null,
    @SerialName("c_nonce_expires_in")
    val cNonceExpiresIn: Int? = null,
    @SerialName("notification_id")
    val notificationId: String? = null,
)
