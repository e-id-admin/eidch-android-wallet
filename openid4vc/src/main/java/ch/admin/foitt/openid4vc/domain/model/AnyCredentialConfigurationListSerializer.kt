package ch.admin.foitt.openid4vc.domain.model

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

internal object AnyCredentialConfigurationListSerializer :
    JsonTransformingSerializer<List<AnyCredentialConfiguration>>(
        tSerializer = ListSerializer(AnyCredentialConfiguration.serializer())
    ) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val credentials = element.jsonObject.map { entry ->
            buildJsonObject {
                put("identifier", entry.key)
                entry.value.jsonObject.forEach {
                    put(it.key, it.value)
                }
            }
        }
        return JsonArray(credentials)
    }
}
