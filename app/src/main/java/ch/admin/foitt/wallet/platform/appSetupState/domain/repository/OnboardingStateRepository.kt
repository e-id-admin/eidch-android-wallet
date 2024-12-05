package ch.admin.foitt.wallet.platform.appSetupState.domain.repository

interface OnboardingStateRepository {
    suspend fun saveOnboardingState(isCompleted: Boolean)
    suspend fun getOnboardingState(): Boolean
}
