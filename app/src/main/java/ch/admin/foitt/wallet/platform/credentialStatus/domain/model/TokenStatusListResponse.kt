package ch.admin.foitt.wallet.platform.credentialStatus.domain.model

import ch.admin.foitt.wallet.platform.utils.base64StringToByteArray
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream
import java.util.zip.InflaterOutputStream

@Serializable
data class TokenStatusListResponse(
    @SerialName("ttl")
    val timeToLive: Long? = null,
    @SerialName("status_list")
    val statusList: TokenStatusList
)

@Serializable
data class TokenStatusList(
    @SerialName("bits")
    val bits: Int,
    @SerialName("lst")
    val lst: String,
) {
    fun decodeAndDeflate(): ByteArray {
        val zippedData = lst.base64StringToByteArray()
        val zlibOutput = ByteArrayOutputStream()
        InflaterOutputStream(zlibOutput).apply {
            write(zippedData)
            close()
        }
        return zlibOutput.toByteArray()
    }
}
