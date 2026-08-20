package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import com.github.michaelbull.result.getOrThrow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class ClientIdentifierSerializer : KSerializer<ClientIdentifier> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = ClientIdentifier::class.qualifiedName ?: "",
        kind = PrimitiveKind.STRING,
    )

    override fun deserialize(decoder: Decoder): ClientIdentifier {
        val clientId = decoder.decodeString()
        return ClientIdentifier.fromString(clientId).getOrThrow()
    }

    override fun serialize(encoder: Encoder, value: ClientIdentifier) {
        encoder.encodeString("${value.clientIdPrefix.value}:${value.clientId}")
    }
}
