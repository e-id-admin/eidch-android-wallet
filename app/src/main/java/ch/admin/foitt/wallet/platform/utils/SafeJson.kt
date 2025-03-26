package ch.admin.foitt.wallet.platform.utils

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import timber.log.Timber
import javax.inject.Inject

class SafeJson @Inject constructor(
    val json: Json
) {
    inline fun <reified T> safeDecodeStringTo(
        string: String,
    ): Result<T, JsonParsingError> = runSuspendCatching {
        json.decodeFromString<T>(string)
    }.mapError { throwable ->
        throwable.toJsonError("safeDecodeStringTo error")
    }

    inline fun <reified T> safeDecodeElementTo(
        jsonElement: JsonElement,
    ): Result<T, JsonParsingError> = runSuspendCatching {
        json.decodeFromJsonElement<T>(jsonElement)
    }.mapError { throwable ->
        throwable.toJsonError("safeDecodeElementTo error")
    }
}

interface JsonError {
    data class Unexpected(val throwable: Throwable) : JsonParsingError
}

sealed interface JsonParsingError

fun Throwable.toJsonError(message: String): JsonParsingError {
    Timber.e(t = this, message = message)
    return JsonError.Unexpected(this)
}
