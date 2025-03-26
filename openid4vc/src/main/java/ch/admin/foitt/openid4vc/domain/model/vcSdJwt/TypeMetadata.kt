package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * reference: https://www.ietf.org/archive/id/draft-ietf-oauth-sd-jwt-vc-05.html#name-display-metadata
 */
@Serializable
data class TypeMetadata(
    @SerialName("vct")
    val vct: String,
    @SerialName("vct#integrity")
    val vctIntegrity: String?,
    @SerialName("name")
    val name: String?,
    @SerialName("description")
    val description: String?,
    @SerialName("extends")
    val extends: String?,
    @SerialName("display")
    val display: List<TypeMetadataDisplay>?,
    @SerialName("claims")
    val claims: List<ClaimMetadata>?,
    @SerialName("schema")
    val schema: String?, // not supported by swiss profile
    @SerialName("schemaUrl")
    val schemaUrl: String?,
    @SerialName("schema_url#integrity")
    val schemaUrlIntegrity: String?,
)

@Serializable
data class TypeMetadataDisplay(
    @SerialName("lang")
    val lang: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String?,
    @Serializable(with = RenderingSerializer::class)
    @SerialName("rendering")
    val rendering: List<Rendering>?,
)

@Serializable(RenderingItemSerializer::class)
sealed class Rendering {
    @SerialName(RENDERING_METHOD_IDENTIFIER)
    abstract val name: String

    @Serializable
    data class Simple(
        override val name: String = RENDERING_METHOD_SIMPLE_KEY,
        @SerialName("logo")
        val logo: LogoMetadata?,
        @SerialName("background_color")
        val backgroundColor: String?,
        @SerialName("text_color")
        val textColor: String?,
    ) : Rendering() {
        companion object {
            const val RENDERING_METHOD_SIMPLE_KEY = "simple"
        }
    }

    @Serializable
    data class SvgTemplates(
        override val name: String = RENDERING_METHOD_SVG_TEMPLATE_KEY,
        @SerialName("uri")
        val uri: String,
        @SerialName("uri#integrity")
        val uriIntegrity: String?,
        @SerialName("properties")
        val properties: SvgTemplateProperties,
    ) : Rendering() {
        companion object {
            const val RENDERING_METHOD_SVG_TEMPLATE_KEY = "svg_templates"
        }
    }

    companion object {
        const val RENDERING_METHOD_IDENTIFIER = "identifier"
    }
}

@Serializable
data class LogoMetadata(
    @SerialName("uri")
    val uri: String,
    @SerialName("uri#integrity")
    val uriIntegrity: String?,
    @SerialName("alt_text")
    val altText: String?,
)

@Serializable
data class SvgTemplateProperties(
    @SerialName("orientation")
    val orientation: SvgLogoOrientation?,
    @SerialName("color_scheme")
    val colorScheme: SvgLogoColorScheme?,
    @SerialName("contrast")
    val contrast: SvgLogoContrast?,
)

@Serializable
enum class SvgLogoOrientation {
    @SerialName("portrait")
    PORTRAIT,

    @SerialName("landscape")
    LANDSCAPE
}

@Serializable
enum class SvgLogoColorScheme {
    @SerialName("light")
    LIGHT,

    @SerialName("dark")
    DARK
}

@Serializable
enum class SvgLogoContrast {
    @SerialName("high")
    HIGH,

    @SerialName("low")
    LOW
}

@Serializable
data class ClaimMetadata(
    @SerialName("path")
    val paths: List<String?>, // must be a claim name (=string), non-negative integer or null
    @SerialName("display")
    val display: List<ClaimDisplayMetadata>?,
    @SerialName("sd")
    val sd: ClaimSelectiveDisclosureMetadata? = ClaimSelectiveDisclosureMetadata.ALLOWED,
    @SerialName("svg_id")
    val svgId: String?,
)

@Serializable
data class ClaimDisplayMetadata(
    @SerialName("lang")
    val lang: String,
    @SerialName("label")
    val label: String,
    @SerialName("description")
    val description: String?,
)

@Serializable
enum class ClaimSelectiveDisclosureMetadata {
    @SerialName("always")
    ALWAYS,

    @SerialName("allowed")
    ALLOWED,

    @SerialName("never")
    NEVER,
}
