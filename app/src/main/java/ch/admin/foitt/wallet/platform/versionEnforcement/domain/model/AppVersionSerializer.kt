package ch.admin.foitt.wallet.platform.versionEnforcement.domain.model

import ch.admin.foitt.wallet.platform.utils.AppVersion
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class AppVersionSerializer : KSerializer<AppVersion> {
    private val delegateSerializer = String.serializer()
    override val descriptor = PrimitiveSerialDescriptor("AppVersion", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: AppVersion) {
        encoder.encodeSerializableValue(delegateSerializer, value.rawVersion)
    }

    override fun deserialize(decoder: Decoder): AppVersion {
        val rawVersion = decoder.decodeSerializableValue(delegateSerializer)
        return AppVersion(rawVersion)
    }
}
