package ch.admin.foitt.wallet.platform.utils

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

class SafeJson @Inject constructor(
    val json: Json
) {

    inline fun <reified T> safeDecodeStringTo(
        string: String,
    ): Result<T, JsonParsingError> = runSuspendCatching {
        json.decodeFromString<T>(string)
    }.mapError(Throwable::toJsonError)
}

interface JsonError {
    data class Unexpected(val throwable: Throwable) : JsonParsingError
}

sealed interface JsonParsingError

fun Throwable.toJsonError(): JsonParsingError {
    Timber.e(this)
    return JsonError.Unexpected(this)
}
