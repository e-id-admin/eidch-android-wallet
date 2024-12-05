package ch.admin.foitt.openid4vc.domain.model.sdjwt

import ch.admin.foitt.openid4vc.utils.base64StringToByteArray
import ch.admin.foitt.openid4vc.utils.createDigest
import com.nimbusds.jwt.JWTClaimNames
import com.nimbusds.jwt.SignedJWT
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import java.time.Instant

data class SdJwt(val payload: String) {

    val signedJWT: SignedJWT by lazy {
        val jwt = matchGroups[JWT]?.value ?: ""
        SignedJWT.parse(jwt)
    }

    val claims: Map<String, SdJwtClaim> by lazy {
        matchGroups[DISCLOSURES]?.value?.let { disclosures ->
            parseDisclosedClaims(disclosures, digestAlgorithm)
        } ?: emptyMap()
    }

    val json: JsonElement by lazy {
        replaceDigestsWithClaims(jwtPayload, claims)
    }

    val expiredAt: Instant? by lazy {
        getInstantClaim(JWTClaimNames.EXPIRATION_TIME)
    }

    val issuedAt: Instant? by lazy {
        getInstantClaim(JWTClaimNames.ISSUED_AT)
    }

    val activatedAt: Instant? by lazy {
        getInstantClaim(JWTClaimNames.NOT_BEFORE)
    }

    val issuer: String by lazy {
        signedJWT.jwtClaimsSet.issuer ?: error("Null or invalid issuer")
    }

    private val matchGroups: MatchNamedGroupCollection by lazy {
        val matchResult = Regex(SD_JWT_PATTERN).matchEntire(payload)
        matchResult?.groups?.let { groupCollection ->
            groupCollection as MatchNamedGroupCollection
        } ?: error("Could not parse SdJwt from payload: $payload")
    }

    private val jwtPayload: JsonObject by lazy {
        val payload = signedJWT.payload.toString()
        Json.parseToJsonElement(payload).jsonObject
    }

    private val digestAlgorithm: String by lazy {
        val algorithm = jwtPayload[ALGORITHM_KEY]
        algorithm?.jsonPrimitive?.content ?: DEFAULT_DIGEST_ALGORITHM
    }

    private fun parseDisclosedClaims(
        disclosuresString: String,
        digestAlgorithm: String
    ): Map<String, SdJwtClaim> {
        val disclosures = disclosuresString.trim(SD_JWT_SEPARATOR).split(SD_JWT_SEPARATOR)
        return disclosures.toSet().associate { disclosure ->
            val digest = disclosure.createDigest(digestAlgorithm)
            val (key, value) = parseDisclosure(disclosure)
            val claim = SdJwtClaim(
                key = key,
                value = value,
                disclosure = disclosure,
            )
            digest to claim
        }
    }

    private fun parseDisclosure(disclosure: String): Pair<String, JsonElement> {
        val decoded = disclosure.base64StringToByteArray()
        val jsonString = String(decoded)
        val array = Json.parseToJsonElement(jsonString).jsonArray
        if (array.size != 3) {
            error("Invalid disclosure: $disclosure")
        }
        return array[1].jsonPrimitive.content to array[2]
    }

    private fun replaceDigestsWithClaims(
        key: String,
        jsonElement: JsonElement,
        claims: Map<String, SdJwtClaim>
    ): JsonElement = when (jsonElement) {
        is JsonObject -> replaceDigestsWithClaims(jsonElement, claims)
        is JsonArray -> JsonArray(
            jsonElement.map { element ->
                replaceDigestsWithClaims(key, element, claims)
            }
        )
        else -> jsonElement
    }

    private fun replaceDigestsWithClaims(
        jsonObject: JsonObject,
        claims: Map<String, SdJwtClaim>
    ): JsonElement {
        val digestsJsonElement = jsonObject.firstNotNullOfOrNull { element ->
            if (element.key == DIGESTS_KEY) element else null
        }?.let { entry ->
            val digests = parseDigestArray(entry.value)
            replaceDigestsAndFindClaims(digests, claims)
        }

        val otherElements = jsonObject.toMutableMap() - DIGESTS_KEY
        val otherElementsWithClaims = otherElements.mapValues { element ->
            replaceDigestsWithClaims(element.key, element.value, claims)
        }
        return JsonObject(otherElementsWithClaims + digestsJsonElement.orEmpty())
    }

    private fun parseDigestArray(digests: JsonElement): List<String> =
        if (digests is JsonArray) {
            digests.jsonArray.map { digest -> digest.jsonPrimitive.content }
        } else {
            error("Invalid digests: $digests")
        }

    private fun replaceDigestsAndFindClaims(
        digests: List<String>,
        claims: Map<String, SdJwtClaim>
    ): JsonObject {
        val elementsWithClaims = digests.mapNotNull { digest -> claims[digest] }
            .associate { claim ->
                claim.key to replaceDigestsWithClaims(claim.key, claim.value, claims)
            }
        return JsonObject(elementsWithClaims)
    }

    private fun getInstantClaim(key: String): Instant? {
        val claim = json.jsonObject[key]
        val content = claim?.jsonPrimitive?.contentOrNull
        return content?.toLong()?.let { seconds ->
            Instant.ofEpochSecond(seconds)
        }
    }

    fun createSelectiveDisclosure(requestedFieldKeys: List<String>): String {
        val disclosures = claims.values
            .filter { it.key in requestedFieldKeys }
            .map { it.disclosure }
        return StringBuilder(signedJWT.parsedString).apply {
            append(SD_JWT_SEPARATOR)
            if (disclosures.isNotEmpty()) {
                disclosures.forEach { disclosure ->
                    append(disclosure)
                    append(SD_JWT_SEPARATOR)
                }
            } else {
                Timber.w(message = "No disclosure for this verification")
            }
        }.toString()
    }

    companion object {
        private const val JWT = "jwt"
        private const val ALGORITHM_KEY = "_sd_alg"
        private const val DISCLOSURES = "disclosures"
        private const val KEYBINDING_JWT = "keyBindingJwt"
        private const val SD_JWT_SEPARATOR = '~'
        private const val SD_JWT_PATTERN = "^" +
            "(?<$JWT>(?<header>[A-Za-z0-9-_]+)\\.(?<body>[A-Za-z0-9-_]+)\\.(?<signature>[A-Za-z0-9-_]+))" + // 1 issuer-signed JWT
            "($SD_JWT_SEPARATOR?" + // 0..1 separators
            "(?<$DISCLOSURES>(([A-Za-z0-9-_]+)$SD_JWT_SEPARATOR)+)?" + // 0..* Disclosures + "~"
            "(?<$KEYBINDING_JWT>([A-Za-z0-9-_]+)\\.([A-Za-z0-9-_]+)\\.([A-Za-z0-9-_]+))?" + // 0..1 Key Binding JWT
            ")\$"

        private const val DIGESTS_KEY = "_sd"
        private const val DEFAULT_DIGEST_ALGORITHM = "SHA-256"
    }
}
