package ch.admin.foitt.wallet.platform.login.domain.usecase

fun interface BiometricLoginEnabled {
    suspend operator fun invoke(): Boolean
}
