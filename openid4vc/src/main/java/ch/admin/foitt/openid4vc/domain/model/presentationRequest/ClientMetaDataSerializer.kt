package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class ClientMetaDataSerializer : KSerializer<ClientMetaData> {

    private val stringToJsonElementSerializer = MapSerializer(String.serializer(), JsonElement.serializer())
    override val descriptor: SerialDescriptor = stringToJsonElementSerializer.descriptor

    override fun deserialize(decoder: Decoder): ClientMetaData {
        require(decoder is JsonDecoder)

        val clientMetaDataMap = decoder.decodeSerializableValue(stringToJsonElementSerializer)
            .map { it.key to it.value }
            .toMap()

        val clientNameList = mutableListOf<ClientName>()
        val logoUriList = mutableListOf<LogoUri>()

        clientMetaDataMap.forEach { entry ->
            when {
                entry.key.contains(CLIENT_NAME) -> {
                    val (content, locale) = entry.key.split(DELIMITER).let {
                        Pair(entry.value.jsonPrimitive.content, it.getOrNull(1) ?: FALLBACK)
                    }
                    clientNameList.add(ClientName(clientName = content, locale = locale))
                }

                entry.key.contains(LOGO_URI) -> {
                    val (content, locale) = entry.key.split(DELIMITER).let {
                        Pair(entry.value.jsonPrimitive.content, it.getOrNull(1) ?: FALLBACK)
                    }
                    logoUriList.add(LogoUri(logoUri = content, locale = locale))
                }
            }
        }

        return ClientMetaData(
            clientNameList = clientNameList,
            logoUriList = logoUriList
        )
    }

    override fun serialize(encoder: Encoder, value: ClientMetaData) {
        require(encoder is JsonEncoder)

        val element = buildJsonObject {
            value.clientNameList.forEach {
                put(if (it.locale != FALLBACK) "$CLIENT_NAME#${it.locale}" else CLIENT_NAME, JsonPrimitive(it.clientName))
            }
            value.logoUriList.forEach {
                put(if (it.locale != FALLBACK) "$LOGO_URI#${it.locale}" else LOGO_URI, JsonPrimitive(it.logoUri))
            }
        }
        encoder.encodeJsonElement(element)
    }

    companion object {
        private const val DELIMITER = "#"
        private const val FALLBACK = "fallback"
        private const val LOGO_URI = "logo_uri"
        private const val CLIENT_NAME = "client_name"
    }
}
