package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EidRequestSubmitFile(
    @SerialName("filename")
    val filename: String,
    @SerialName("hash")
    val hash: String
)
