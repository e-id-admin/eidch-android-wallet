package ch.admin.foitt.wallet.feature.onboarding.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.onboarding.domain.usecase.SaveOnboardingState
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.OnboardingStateRepository
import javax.inject.Inject

class SaveOnboardingStateImpl @Inject constructor(
    private val repository: OnboardingStateRepository,
) : SaveOnboardingState {

    override suspend fun invoke(isCompleted: Boolean) {
        repository.saveOnboardingState(isCompleted = isCompleted)
    }
}
