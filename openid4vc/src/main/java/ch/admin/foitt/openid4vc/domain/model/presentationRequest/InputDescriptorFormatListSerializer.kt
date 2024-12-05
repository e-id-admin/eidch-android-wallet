package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat.VcSdJwt
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
internal class InputDescriptorFormatListSerializer : KSerializer<List<InputDescriptorFormat>> {
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor(InputDescriptorFormat::class.qualifiedName ?: "", PolymorphicKind.SEALED) {
            element(
                (VcSdJwt::class.qualifiedName ?: "") + "Format",
                buildClassSerialDescriptor(VcSdJwt::class.qualifiedName ?: "") {
                    element<String>("algField")
                }
            )
        }

    override fun deserialize(decoder: Decoder): List<InputDescriptorFormat> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be deserialized only by JSON")
        val jsonElement = jsonDecoder.decodeJsonElement()

        if (jsonElement !is JsonObject) {
            throw SerializationException("Expected JsonObject for FormatList deserialization")
        }

        return jsonElement.entries.map { (key, value) ->
            when {
                key == VcSdJwt.VC_SD_JWT_KEY && value is JsonObject ->
                    VcSdJwt.deserialize(value)
                key == VcSdJwt.JWT_VC_KEY && value is JsonObject ->
                    VcSdJwt.deserialize(value)
                else -> error("Unsupported input descriptor format: $decoder")
            }
        }
    }

    override fun serialize(encoder: Encoder, value: List<InputDescriptorFormat>) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("This class can be serialized only by JSON")

        val jsonObject = buildJsonObject {
            value.forEach { format ->
                when (format) {
                    is VcSdJwt -> put(VcSdJwt.VC_SD_JWT_KEY, format.serialize())
                }
            }
        }
        jsonEncoder.encodeJsonElement(jsonObject)
    }
}
