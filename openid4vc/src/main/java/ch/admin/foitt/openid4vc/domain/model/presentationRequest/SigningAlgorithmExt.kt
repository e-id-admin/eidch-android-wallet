package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

internal fun SigningAlgorithm.Companion.deserialize(algorithms: JsonElement): List<SigningAlgorithm> {
    val supportedAlgorithms = if (algorithms is JsonArray) {
        algorithms.jsonArray.mapNotNull { entry ->
            SigningAlgorithm.entries.find { algorithm ->
                algorithm.stdName == entry.jsonPrimitive.contentOrNull
            }
        }
    } else {
        val algorithm = SigningAlgorithm.entries.find { algorithm ->
            algorithm.stdName == algorithms.jsonPrimitive.contentOrNull
        }
        algorithm?.let { listOf(algorithm) } ?: emptyList()
    }
    return supportedAlgorithms
}
