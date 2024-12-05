package ch.admin.foitt.wallet.feature.changeLogin.domain.usecase

fun interface GetCurrentPassphraseAttempts {
    operator fun invoke(): Int
}
