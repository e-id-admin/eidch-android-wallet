package ch.admin.foitt.wallet.platform.login.domain.usecase

fun interface GetRemainingLoginAttempts {
    operator fun invoke(): Int
}
