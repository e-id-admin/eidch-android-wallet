package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.Rendering.Companion.RENDERING_METHOD_IDENTIFIER
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.Rendering.Simple.Companion.RENDERING_METHOD_SIMPLE_KEY
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.Rendering.SvgTemplates.Companion.RENDERING_METHOD_SVG_TEMPLATE_KEY
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.Rendering.VcSdJwtOcaRendering.Companion.RENDERING_METHOD_OCA_KEY
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

internal object RenderingSerializer :
    JsonTransformingSerializer<List<Rendering>>(tSerializer = ListSerializer(Rendering.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val renderings = buildJsonArray {
            element.jsonObject.forEach { rendering ->
                when (rendering.key) {
                    RENDERING_METHOD_SIMPLE_KEY,
                    RENDERING_METHOD_OCA_KEY -> {
                        addJsonObject {
                            put(RENDERING_METHOD_IDENTIFIER, rendering.key)
                            rendering.value.jsonObject.forEach {
                                put(it.key, it.value)
                            }
                        }
                    }
                    RENDERING_METHOD_SVG_TEMPLATE_KEY -> {
                        rendering.value.jsonArray.forEach { svgTemplate ->
                            addJsonObject {
                                put(RENDERING_METHOD_IDENTIFIER, rendering.key)
                                svgTemplate.jsonObject.forEach {
                                    put(it.key, it.value)
                                }
                            }
                        }
                    }
                    else -> error("illegal rendering")
                }
            }
        }
        return JsonArray(renderings)
    }
}
