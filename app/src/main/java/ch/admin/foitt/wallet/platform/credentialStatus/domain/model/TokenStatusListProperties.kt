package ch.admin.foitt.wallet.platform.credentialStatus.domain.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable(CredentialStatusPropertiesSerializer::class)
sealed interface CredentialStatusProperties

class CredentialStatusPropertiesSerializer :
    JsonContentPolymorphicSerializer<CredentialStatusProperties>(CredentialStatusProperties::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CredentialStatusProperties> {
        val jsonObject = element.jsonObject
        return when {
            jsonObject.containsKey("status_list") -> TokenStatusListProperties.serializer()
            else -> error("Unsupported credential status properties")
        }
    }
}

@Serializable
data class TokenStatusListProperties(
    @SerialName("status_list")
    val statusList: StatusList
) : CredentialStatusProperties {
    @Serializable
    data class StatusList(
        @SerialName("idx")
        val index: Int,
        @SerialName("uri")
        val uri: String
    )
}
