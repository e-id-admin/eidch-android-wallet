package ch.admin.foitt.openid4vc.utils

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import timber.log.Timber
import javax.inject.Inject

internal class SafeJson @Inject constructor(
    private val json: Json
) {

    inline fun <reified T> safeDecodeStringTo(
        string: String
    ): Result<T, JsonParsingError> = runSuspendCatching {
        json.decodeFromString<T>(string)
    }.mapError { throwable ->
        throwable.toJsonError("SafeJson decodeFromString error")
    }

    inline fun <reified T> safeEncodeObjectToString(objectToEncode: T): Result<String, JsonParsingError> = runSuspendCatching {
        json.encodeToString(objectToEncode)
    }.mapError { throwable ->
        throwable.toJsonError("SafeJson encodeToString error")
    }

    fun safeDecodeToJsonObject(
        string: String,
    ): Result<JsonObject, JsonParsingError> = runSuspendCatching {
        json.parseToJsonElement(string).jsonObject
    }.mapError { throwable ->
        throwable.toJsonError("SafeJson parseToJsonElement error")
    }
}

internal interface JsonError {
    data class Unexpected(val throwable: Throwable) : JsonParsingError
}

internal sealed interface JsonParsingError

internal fun Throwable.toJsonError(message: String): JsonParsingError {
    Timber.e(t = this, message = message)
    return JsonError.Unexpected(this)
}
