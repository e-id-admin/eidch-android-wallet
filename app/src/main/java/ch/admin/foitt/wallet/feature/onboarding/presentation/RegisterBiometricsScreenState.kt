package ch.admin.foitt.wallet.feature.onboarding.presentation

sealed interface RegisterBiometricsScreenState {
    data object Initial : RegisterBiometricsScreenState
    data object Available : RegisterBiometricsScreenState
    data object Lockout : RegisterBiometricsScreenState
    data object Error : RegisterBiometricsScreenState
    data object DisabledOnDevice : RegisterBiometricsScreenState
    data object DisabledForApp : RegisterBiometricsScreenState
}
