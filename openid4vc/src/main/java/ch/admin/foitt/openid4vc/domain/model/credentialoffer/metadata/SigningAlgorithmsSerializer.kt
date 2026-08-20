package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

internal class SigningAlgorithmsSerializer : JsonTransformingSerializer<List<SigningAlgorithm>>(
    tSerializer = ListSerializer(SigningAlgorithm.serializer())
) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val supportedAlgorithms = element.jsonArray.mapNotNull { entry ->
            entry.jsonPrimitive.contentOrNull
                ?.let(SigningAlgorithm::fromStdName)
                ?.let { algorithm -> JsonPrimitive(algorithm.stdName) }
        }
        return JsonArray(supportedAlgorithms)
    }
}
