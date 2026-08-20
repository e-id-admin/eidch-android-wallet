package ch.admin.foitt.openid4vc.utils

import ch.admin.foitt.openid4vc.domain.model.DigestAlgorithm
import java.net.URL
import java.security.MessageDigest
import java.util.Base64

internal fun String.createDigest(algorithm: DigestAlgorithm): String {
    val digest = MessageDigest.getInstance(algorithm.stdName)
    val bytes = digest.digest(toByteArray(Charsets.ISO_8859_1))
    return bytes.toBase64StringUrlEncodedWithoutPadding()
}

fun String.base64ToDecodedString() = String(Base64.getDecoder().decode(this))

/**
 * Collapses empty path segments in a full URL string, e.g.
 * `https://example.com/issuer1//.well-known` -> `https://example.com/issuer1/.well-known`.
 *
 * Caveats:
 * - As a side effect a trailing slash is also removed.
 * - Rebuilds the URL from scheme/host/port/path/query only: any userinfo (`user:pass@`) or
 *   fragment (`#...`) is dropped.
 * - Expects a valid absolute URL; throws [java.net.MalformedURLException] otherwise.
 */
fun String.removeEmptyPathSegments(): String {
    val url = URL(this)
    val cleanPath = url.path.split("/")
        .filter { it.isNotEmpty() }
        .joinToString(separator = "/", prefix = "/")
    return URL(url.protocol, url.host, url.port, cleanPath + (url.query?.let { "?$it" } ?: "")).toString()
}
