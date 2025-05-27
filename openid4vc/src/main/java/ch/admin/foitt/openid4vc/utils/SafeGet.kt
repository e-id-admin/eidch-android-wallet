package ch.admin.foitt.openid4vc.utils

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import java.net.URL

fun safeGetUrl(urlString: String?): Result<URL, SafeGetUrlError> = runSuspendCatching {
    URL(urlString)
}.mapError {
    // do not log this error to dynatrace to not spam it
    SafeGetError.Unexpected
}

interface SafeGetError {
    data object Unexpected : SafeGetUrlError
}

sealed interface SafeGetUrlError
