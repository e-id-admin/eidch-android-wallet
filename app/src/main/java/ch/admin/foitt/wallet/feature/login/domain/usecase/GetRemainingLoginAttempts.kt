package ch.admin.foitt.wallet.feature.login.domain.usecase

fun interface GetRemainingLoginAttempts {
    operator fun invoke(): Int
}
