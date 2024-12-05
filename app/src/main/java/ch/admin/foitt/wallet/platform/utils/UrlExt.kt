package ch.admin.foitt.wallet.platform.utils

import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Searches the query string for the first value with the given key.
 *
 * @param key which will be encoded
 * @return the decoded value or null if no parameter is found
 */
fun URL.getQueryParameter(key: String): String? {
    val query: String = this.query ?: return null
    val encodedKey = URLEncoder.encode(key, Charsets.UTF_8.name())
    val length = query.length
    var start = 0
    do {
        val nextAmpersand = query.indexOf('&', start)
        val end = if (nextAmpersand != -1) nextAmpersand else length
        var separator = query.indexOf('=', start)
        if (separator > end || separator == -1) {
            separator = end
        }
        if (separator - start == encodedKey.length &&
            query.regionMatches(start, encodedKey, 0, encodedKey.length)
        ) {
            return if (separator == end) {
                ""
            } else {
                val encodedValue = query.substring(separator + 1, end)
                URLDecoder.decode(encodedValue, Charsets.UTF_8.name())
            }
        }

        // Move start to end of name.
        start = if (nextAmpersand != -1) {
            nextAmpersand + 1
        } else {
            break
        }
    } while (true)
    return null
}
