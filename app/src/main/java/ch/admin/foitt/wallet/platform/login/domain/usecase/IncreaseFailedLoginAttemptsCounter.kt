package ch.admin.foitt.wallet.platform.login.domain.usecase

fun interface IncreaseFailedLoginAttemptsCounter {
    operator fun invoke()
}
