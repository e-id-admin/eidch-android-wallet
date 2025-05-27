package ch.admin.foitt.wallet.platform.oca.domain.model

import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.DataSourceOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LabelOverlay
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.LabelOverlay1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import timber.log.Timber

typealias AttributeKey = String
typealias Locale = String
typealias DataSourceFormat = String
typealias JsonPath = String

@Serializable
data class OcaBundle(
    @SerialName("capture_bases") val captureBases: List<CaptureBase>,
    @SerialName("overlays") val overlays: List<Overlay>,
) {
    val attributes = getAllAttributes()

    private fun getAllAttributes(): List<OverlayBundleAttribute> = captureBases.flatMap { captureBase ->
        val labelOverlays = getLabelsForAttributes(captureBase)
        val dataSources = getDataSourcesForAttributes(captureBase)
        captureBase.attributes.map { attribute ->
            OverlayBundleAttribute(
                captureBaseDigest = captureBase.digest,
                name = attribute.key,
                attributeType = attribute.value,
                labels = labelOverlays.getOrDefault(attribute.key, emptyMap()),
                dataSources = dataSources.getOrDefault(
                    attribute.key,
                    emptyMap()
                )
            )
        }
    }

    private fun getLabelsForAttributes(captureBase: CaptureBase): Map<AttributeKey, Map<Locale, String>> {
        val labelOverlays = getLatestOverlaysOfType<LabelOverlay>(captureBase.digest)
        val labels = captureBase.attributes.keys.associateWith { attribute ->
            labelOverlays.mapNotNull { overlay ->
                when (overlay) {
                    is LabelOverlay1x0 -> {
                        overlay.attributeLabels[attribute]?.let {
                            overlay.language to it
                        }
                    }
                }
            }.toMap()
        }

        if (labels.any { labelOverlays.size > it.value.size }) {
            Timber.w("Duplicate in label overlays")
        }

        return labels
    }

    private fun getDataSourcesForAttributes(captureBase: CaptureBase): Map<AttributeKey, Map<DataSourceFormat, JsonPath>> {
        val dataSourceOverlays = getLatestOverlaysOfType<DataSourceOverlay>(captureBase.digest)
        val dataSources = captureBase.attributes.keys.associateWith { attribute ->
            dataSourceOverlays.mapNotNull { overlay ->
                when (overlay) {
                    is DataSourceOverlay1x0 -> {
                        overlay.attributeSources[attribute]?.let { jsonPathString ->
                            overlay.format to jsonPathString
                        }
                    }
                }
            }.toMap()
        }

        if (dataSources.any { dataSourceOverlays.size > it.value.size }) {
            Timber.w("Duplicate in data source overlays")
        }

        return dataSources
    }

    /**
     * Retrieves a specific Overlay Bundle attribute from the Capture Base.
     *
     * @param name The name of the Capture Base attribute.
     * @param digest An CESR digest of the associated Capture Base.
     * @return A [OverlayBundleAttribute] representing the Capture Base attributes with supplementary information from Overlays. When attribute is not found, `null` is returned.
     */
    fun getAttribute(name: String, digest: String): OverlayBundleAttribute? {
        return attributes.firstOrNull { it.captureBaseDigest == digest && it.name == name }
    }

    /**
     * Retrieves all Overlay Bundle attributes.
     *
     * @param digest An optional CESR digest of the associated Capture Base. Default: attributes for all Capture Base digests are considered.
     * @return A list of [OverlayBundleAttribute] representing the Capture Base attributes with supplementary information from Overlays.
     */
    private fun getAttributes(digest: String? = null): List<OverlayBundleAttribute> {
        return if (digest == null) {
            attributes
        } else {
            attributes.filter { it.captureBaseDigest == digest }
        }
    }

    /**
     * Retrieves Overlay Bundle attribute associated to their JSONPath for the data source format.
     *
     * @param dataSourceFormat The format identifier for data source mapping, e.g. "vc+sd-jwt"
     * @param digest An optional CESR digest of the associated Capture Base. Default: attributes for all Capture Base digests are considered.
     * @return A map of JSONPath (pointing to data source property) to [OverlayBundleAttribute] (for associated Capture Base)
     */
    fun getAttributesForDataSourceFormat(
        dataSourceFormat: DataSourceFormat,
        digest: String?
    ): Map<JsonPath, OverlayBundleAttribute> {
        return getAttributes(digest = digest)
            .mapNotNull { attribute ->
                val jsonPath = attribute.dataSources[dataSourceFormat] ?: return@mapNotNull null
                jsonPath to attribute
            }.toMap()
    }

    fun getAttributeForJsonPath(jsonPath: String) = getAttributes().find { attribute ->
        val normalizedJsonPath = jsonPath.toDotNotation()
        attribute.dataSources.values.any { dataSourceJsonPath: String ->
            val normalizedDataSourceJsonPath = dataSourceJsonPath.toDotNotation()
            normalizedDataSourceJsonPath == normalizedJsonPath || validateJsonPaths(normalizedJsonPath, normalizedDataSourceJsonPath)
        }
    }

    private fun String.toDotNotation(): String {
        return this
            // Replace all bracket notations with dot notation, e.g. foo["bar"] -> foo.bar
            .replace(regex = bracketNotationRegex, replacement = ".\${selector}")
            // Replace all wildcard dot notations with array notation, e.g. foo.* -> foo[*]
            .replace(regex = wildCardRegex, replacement = "[\${wildcard}]")
    }

    @Suppress("ReturnCount")
    private fun validateJsonPaths(inputJsonPath: String, validationJsonPath: String): Boolean {
        val inputPathSplit = inputJsonPath.split(".")
        val validationPathSplit = validationJsonPath.split(".")
        if (inputPathSplit.size != validationPathSplit.size) {
            return false
        }
        inputPathSplit.forEachIndexed { index, inputPathPart ->
            val validationPathPart = validationPathSplit[index]
            if (inputPathPart != validationPathPart && !areSameArray(inputPathPart, validationPathPart)) return false
        }
        return true
    }

    @Suppress("ReturnCount")
    private fun areSameArray(inputJsonPathArray: String, validationJsonPathArray: String): Boolean {
        val matchInputArray = arrayRegex.matchEntire(inputJsonPathArray) ?: return false
        val matchValidationArray = arrayWildcardRegex.matchEntire(validationJsonPathArray) ?: return false

        return matchInputArray.groups[1] == matchValidationArray.groups[1] && matchValidationArray.groups[2]?.value == "*"
    }

    /**
     * Retrieves Overlays for latest version by type and digest.
     * - Parameters:
     * - Overlay: The specific Overlay to look for. Must be a specific Overlay interface or class, e.g. [LabelOverlay] or [LabelOverlay1x0].
     * - digest: An optional CESR digest of the associated Capture Base. Default: All Capture Bases
     * - Returns: The list of matching [Overlay]s.
     */
    inline fun <reified T : Overlay> getLatestOverlaysOfType(digest: String? = null): List<T> {
        val overlaysOfType = overlays.filterIsInstance<T>()
        val latestOverlayType = overlaysOfType
            .map { it.type }
            .distinct()
            .maxByOrNull { specType ->
                val versionString = specType.type.split("/").last() // e.g., "1.2.3"
                OverlayVersion(versionString)
            }

        return overlaysOfType.filter { overlay ->
            overlay.type == latestOverlayType
        }.let { filteredOverlays ->
            digest?.let { digest ->
                filteredOverlays.filter { digest == it.captureBaseDigest }
            } ?: filteredOverlays
        }
    }

    companion object {
        // matches: arrayName[*]
        private val arrayWildcardRegex = Regex("""(\w+)\[(\*)]""")

        // matches: arrayName[index]
        private val arrayRegex = Regex("""(\w+)\[(\d+)]""")

        // matches: bracket notation
        private val bracketNotationRegex = """\[(?<quote>["'])(?<selector>\w+)\k<quote>]""".toRegex()

        // matches: dot notation wildcard
        private val wildCardRegex = """\.(?<wildcard>\*)""".toRegex()
    }
}
