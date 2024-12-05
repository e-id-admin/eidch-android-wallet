package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat.VcSdJwt
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
internal class InputDescriptorFormatSerializer : KSerializer<InputDescriptorFormat> {
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor(InputDescriptorFormat::class.qualifiedName ?: "", PolymorphicKind.SEALED) {
            element(
                (VcSdJwt::class.qualifiedName ?: "") + "Format",
                buildClassSerialDescriptor(VcSdJwt::class.qualifiedName ?: "") {
                    element<String>("algField")
                }
            )
        }

    override fun deserialize(decoder: Decoder): InputDescriptorFormat {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        val inputDescriptor = element.jsonObject[VcSdJwt.VC_SD_JWT_KEY]?.jsonObject
            ?: element.jsonObject[VcSdJwt.JWT_VC_KEY]?.jsonObject
            ?: error("Unsupported input descriptor format: $decoder")

        return VcSdJwt.deserialize(inputDescriptor)
    }

    override fun serialize(encoder: Encoder, value: InputDescriptorFormat) {
        require(encoder is JsonEncoder)
        val element = when (value) {
            is VcSdJwt -> buildJsonObject {
                put(VcSdJwt.VC_SD_JWT_KEY, value.serialize())
            }
        }
        encoder.encodeJsonElement(element)
    }
}
