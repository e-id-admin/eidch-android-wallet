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
    }.mapError(Throwable::toJsonError)

    inline fun <reified T> safeEncodeObjectToString(objectToEncode: T): Result<String, JsonParsingError> = runSuspendCatching {
        json.encodeToString(objectToEncode)
    }.mapError(Throwable::toJsonError)

    fun safeDecodeToJsonObject(
        string: String,
    ): Result<JsonObject, JsonParsingError> = runSuspendCatching {
        json.parseToJsonElement(string).jsonObject
    }.mapError(Throwable::toJsonError)
}

internal interface JsonError {
    data class Unexpected(val throwable: Throwable) : JsonParsingError
}

internal sealed interface JsonParsingError

internal fun Throwable.toJsonError(): JsonParsingError {
    Timber.e(this)
    return JsonError.Unexpected(this)
}
