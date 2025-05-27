package ch.admin.foitt.wallet.platform.oca.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import timber.log.Timber

@Serializable(with = AttributeTypeSerializer::class)
sealed class AttributeType {
    @Serializable
    data object Binary : AttributeType()

    @Serializable
    data object Boolean : AttributeType()

    @Serializable
    data object DateTime : AttributeType()

    @Serializable
    data object Numeric : AttributeType()

    @Serializable
    data object Text : AttributeType()

    @Serializable
    data class Reference(val captureBaseReference: String) : AttributeType()

    @Serializable
    data class Array(val attributeType: AttributeType) : AttributeType()
}

internal object AttributeTypeSerializer : KSerializer<AttributeType> {
    override val descriptor = PrimitiveSerialDescriptor("AttributeType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: AttributeType) {
        // not implemented
    }

    override fun deserialize(decoder: Decoder): AttributeType {
        require(decoder is JsonDecoder)
        val value = decoder.decodeString()
        return determineType(value)
    }

    private fun determineType(value: String): AttributeType = when {
        value == "Binary" -> AttributeType.Binary
        value == "Boolean" -> AttributeType.Boolean
        value == "DateTime" -> AttributeType.DateTime
        value == "Numeric" -> AttributeType.Numeric
        value == "Text" -> AttributeType.Text
        value.startsWith("refs:") -> AttributeType.Reference(value.toReference())
        value.startsWith("Array[") -> AttributeType.Array(determineType(value.removeSurrounding("Array[", "]")))
        else -> {
            Timber.w("OCA contains unsupported attribute type: $value")
            error("unknown attribute type")
        }
    }

    private fun String.toReference(): String = split("refs:").getOrElse(1) { error("No valid reference in $this") }
}
