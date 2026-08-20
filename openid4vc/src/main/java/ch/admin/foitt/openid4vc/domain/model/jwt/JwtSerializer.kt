package ch.admin.foitt.openid4vc.domain.model.jwt

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object JwtSerializer : KSerializer<Jwt> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Jwt", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Jwt) {
        encoder.encodeString(value.rawJwt)
    }

    override fun deserialize(decoder: Decoder): Jwt {
        return Jwt(decoder.decodeString())
    }
}
