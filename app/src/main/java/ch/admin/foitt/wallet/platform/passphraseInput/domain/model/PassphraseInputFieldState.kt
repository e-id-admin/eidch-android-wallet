package ch.admin.foitt.wallet.platform.passphraseInput.domain.model

sealed class PassphraseInputFieldState {
    data object Typing : PassphraseInputFieldState()
    data object Success : PassphraseInputFieldState()
    data object Error : PassphraseInputFieldState()
}
