package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat.VcSdJwt.Companion.VC_SD_JWT_KEY
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addAll
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray

internal fun InputDescriptorFormat.VcSdJwt.Companion.deserialize(inputDescriptorJson: JsonObject): InputDescriptorFormat.VcSdJwt {
    val sdJwtAlgorithms = inputDescriptorJson[SDJWT_ALGORITHM_KEY]
        ?: inputDescriptorJson[JWT_VC_ALGORITHM_KEY]

    requireNotNull(sdJwtAlgorithms)
    val kbJwtAlgorithms = inputDescriptorJson[KBJWT_ALGORITHM_KB_KEY]

    val supportedSdJwtAlgorithms = SigningAlgorithm.deserialize(sdJwtAlgorithms)
    val supportedKbJwtAlgorithms = kbJwtAlgorithms?.let { SigningAlgorithm.deserialize(kbJwtAlgorithms) }

    return InputDescriptorFormat.VcSdJwt(
        name = VC_SD_JWT_KEY,
        sdJwtAlgorithms = supportedSdJwtAlgorithms,
        kbJwtAlgorithms = supportedKbJwtAlgorithms,
    )
}

@OptIn(ExperimentalSerializationApi::class)
internal fun InputDescriptorFormat.VcSdJwt.serialize(): JsonObject =
    buildJsonObject {
        putJsonArray(InputDescriptorFormat.VcSdJwt.SDJWT_ALGORITHM_KEY) {
            val algorithms = sdJwtAlgorithms.map { it.stdName }
            addAll(algorithms)
        }
        putJsonArray(InputDescriptorFormat.VcSdJwt.KBJWT_ALGORITHM_KB_KEY) {
            val algorithms = kbJwtAlgorithms?.map { it.stdName }
            algorithms?.let { addAll(algorithms) }
        }
    }
