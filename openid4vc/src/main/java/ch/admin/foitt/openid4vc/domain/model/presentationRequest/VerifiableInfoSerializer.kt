package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class VerifiableInfoSerializer : KSerializer<List<VerifierInfo>?> {
    private val delegate = ListSerializer(VerifierInfo.serializer()).nullable
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): List<VerifierInfo>? {
        return decoder.decodeSerializableValue(delegate)
    }

    override fun serialize(encoder: Encoder, value: List<VerifierInfo>?) {
        encoder.encodeSerializableValue(delegate, value)
    }
}
