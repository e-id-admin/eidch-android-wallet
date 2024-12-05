package ch.admin.foitt.wallet.feature.changeLogin.domain.usecase

fun interface GetNewPassphraseConfirmationAttempts {
    operator fun invoke(): Int
}
