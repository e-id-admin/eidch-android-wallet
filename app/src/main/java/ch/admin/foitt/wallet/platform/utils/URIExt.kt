package ch.admin.foitt.wallet.platform.utils

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import java.net.URI
import java.net.URLDecoder

/**
 * Returns the first query parameter value for the given [key]. The value is UTF-8 decoded. If the query parameter has no value, an empty string is returned.
 * Returns null if
 * - the key cannot be found in the query
 * - the key is blank
 * - the URI does not contain a query
 */
fun URI.getQueryParameter(key: String): Result<String?, Throwable> = runSuspendCatching {
    if (rawQuery == null || key.isBlank()) {
        return@runSuspendCatching null
    }
    return@runSuspendCatching rawQuery
        .split("&")
        .firstOrNull {
            val queryKey = URLDecoder.decode(it.substringBefore("="), "utf-8")
            queryKey.equals(key)
        }?.let {
            URLDecoder.decode(it.substringAfter("=", ""), "utf-8")
        }
}
