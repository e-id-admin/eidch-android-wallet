package ch.admin.foitt.wallet.feature.onboarding

import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.OnboardingStateRepository
import javax.inject.Inject

class FakeOnboardingStateRepository @Inject constructor() : OnboardingStateRepository {

    var onboarded = false

    override suspend fun getOnboardingState() = onboarded

    override suspend fun saveOnboardingState(isCompleted: Boolean) {
        onboarded = isCompleted
    }
}
