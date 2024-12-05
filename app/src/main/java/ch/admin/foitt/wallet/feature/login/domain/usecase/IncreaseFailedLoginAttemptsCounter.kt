package ch.admin.foitt.wallet.feature.login.domain.usecase

fun interface IncreaseFailedLoginAttemptsCounter {
    operator fun invoke()
}
