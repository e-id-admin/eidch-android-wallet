package ch.admin.foitt.wallet.platform.crypto.domain.model

sealed interface HashDataError {
    val throwable: Throwable
    class Unexpected(override val throwable: Throwable) : HashDataError
}
