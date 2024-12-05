package ch.admin.foitt.wallet.platform.navArgs.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresentationSuccessNavArg(
    @SerialName("sent_fields")
    val sentFields: Array<String>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PresentationSuccessNavArg

        return sentFields.contentEquals(other.sentFields)
    }

    override fun hashCode(): Int {
        return sentFields.contentHashCode()
    }
}
