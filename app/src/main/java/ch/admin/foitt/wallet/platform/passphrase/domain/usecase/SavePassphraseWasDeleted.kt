package ch.admin.foitt.wallet.platform.passphrase.domain.usecase

fun interface SavePassphraseWasDeleted {
    suspend operator fun invoke(passphraseWasDeleted: Boolean)
}
