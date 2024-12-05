package ch.admin.foitt.wallet.feature.onboarding.domain.usecase

interface SaveOnboardingState {
    suspend operator fun invoke(isCompleted: Boolean)
}
