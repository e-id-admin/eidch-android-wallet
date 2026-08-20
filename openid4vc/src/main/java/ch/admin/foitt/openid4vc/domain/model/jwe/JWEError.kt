package ch.admin.foitt.openid4vc.domain.model.jwe

sealed interface JWEError {
    data class Unexpected(val throwable: Throwable) : DecryptJWEError, CreateJWEError
    data object PayloadSizeExceeded : DecryptJWEError
}

sealed interface DecryptJWEError
sealed interface CreateJWEError

internal class PayloadSizeExceededException(message: String) : Exception(message)
