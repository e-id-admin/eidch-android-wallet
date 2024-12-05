package ch.admin.foitt.wallet.platform.passphrase.domain.usecase

fun interface GetPassphraseWasDeleted {
    suspend operator fun invoke(): Boolean
}
