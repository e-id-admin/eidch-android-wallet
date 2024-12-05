package ch.admin.foitt.wallet.platform.scaffold.domain.model

sealed interface ErrorDialogState {
    val errorDetails: String?

    object Closed : ErrorDialogState {
        override val errorDetails = null
    }
    data class Unexpected(
        override val errorDetails: String? = null,
    ) : ErrorDialogState
    data class Network(
        override val errorDetails: String? = null,
    ) : ErrorDialogState
    data class Wallet(
        override val errorDetails: String? = null,
    ) : ErrorDialogState
}
