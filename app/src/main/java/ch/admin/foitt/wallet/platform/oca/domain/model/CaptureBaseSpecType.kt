package ch.admin.foitt.wallet.platform.oca.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CaptureBaseSpecType(val type: String) {
    @SerialName("spec/capture_base/1.0")
    CAPTURE_BASE_1_0("spec/capture_base/1.0");

    companion object {
        fun getByType(type: String?): CaptureBaseSpecType? {
            return CaptureBaseSpecType.entries.firstOrNull { it.type == type }
        }
    }
}
