package ch.admin.foitt.wallet.platform.oca.domain.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = CaptureBaseSerializer::class)
sealed interface CaptureBase {
    @SerialName("type")
    val type: CaptureBaseSpecType

    @SerialName("digest")
    val digest: String

    @SerialName("attributes")
    val attributes: Map<String, AttributeType>
}

private object CaptureBaseSerializer : JsonContentPolymorphicSerializer<CaptureBase>(
    CaptureBase::class
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CaptureBase> {
        return when (CaptureBaseSpecType.getByType(element.jsonObject["type"]?.jsonPrimitive?.content)) {
            CaptureBaseSpecType.CAPTURE_BASE_1_0 -> CaptureBase1x0.serializer()
            null -> throw SerializationException("Unknown capture base type: ${element.jsonObject["type"]?.jsonPrimitive?.content}")
        }
    }
}

// https://oca.colossi.network/specification/v1.0.1.html#capture-base
@Serializable
data class CaptureBase1x0(
    @SerialName("digest")
    override val digest: String,

    @SerialName("attributes")
    override val attributes: Map<String, AttributeType>,

    @SerialName("classification")
    val classification: String? = null,

    @SerialName("flagged_attributes")
    val flaggedAttributes: List<String> = emptyList()
) : CaptureBase {
    @SerialName("type")
    override val type: CaptureBaseSpecType = CaptureBaseSpecType.CAPTURE_BASE_1_0
}
