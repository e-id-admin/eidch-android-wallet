package ch.admin.foitt.wallet.platform.biometrics.domain.usecase

fun interface SaveUseBiometricLogin {
    suspend operator fun invoke(isEnabled: Boolean)
}
