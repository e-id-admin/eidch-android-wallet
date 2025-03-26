package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.Rendering.Companion.RENDERING_METHOD_IDENTIFIER
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.Rendering.Simple.Companion.RENDERING_METHOD_SIMPLE_KEY
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.Rendering.SvgTemplates.Companion.RENDERING_METHOD_SVG_TEMPLATE_KEY
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object RenderingItemSerializer : JsonContentPolymorphicSerializer<Rendering>(Rendering::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Rendering> {
        val identifier = element.jsonObject[RENDERING_METHOD_IDENTIFIER]?.jsonPrimitive?.content
        return when (identifier) {
            RENDERING_METHOD_SIMPLE_KEY -> Rendering.Simple.serializer()
            RENDERING_METHOD_SVG_TEMPLATE_KEY -> Rendering.SvgTemplates.serializer()
            else -> error("illegal rendering method")
        }
    }
}
