package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object ProtectedIssuanceAuthorizationObjectSerializer : KSerializer<ProtectedIssuanceAuthorizationObject> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ProtectedIssuanceAuthorizationObject")

    override fun deserialize(decoder: Decoder): ProtectedIssuanceAuthorizationObject {
        val json = (decoder as JsonDecoder).decodeJsonElement().jsonObject

        val vct = json["vct"]?.jsonPrimitive?.content
            ?: error("vct field missing")
        val vctName = json["vct_name"]?.jsonPrimitive?.content
            ?: error("vct_name field missing")

        val reason = json.entries.mapNotNull { (key, value) ->
            if (key.startsWith("reason")) {
                val locale = key.split("#").getOrNull(1) ?: DisplayLanguage.FALLBACK
                if (locale.length > 40) error("allowed max length of 40 exceeded")
                val reason = value.jsonPrimitive.content
                if (reason.length > 1000) error("allowed max length of 1000 exceeded")
                Pair(locale, reason)
            } else {
                null
            }
        }.toMap()

        return ProtectedIssuanceAuthorizationObject(
            vct = vct,
            vctName = vctName,
            reason = reason,
        )
    }

    override fun serialize(encoder: Encoder, value: ProtectedIssuanceAuthorizationObject) {
        val jsonEncoder = encoder as JsonEncoder
        val map = buildJsonObject {
            put("vct", value.vct)
            put("vct_name", value.vctName)
            value.reason.forEach { (locale, text) ->
                val key = if (locale == DisplayLanguage.FALLBACK) "reason" else "reason#$locale"
                put(key, text)
            }
        }
        jsonEncoder.encodeJsonElement(map)
    }
}
