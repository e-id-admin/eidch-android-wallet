package ch.admin.foitt.wallet.platform.appSetupState.data.repository

import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.OnboardingStateRepository
import javax.inject.Inject

class OnboardingStateCompletionRepository @Inject constructor(
    private val sharedPreferences: EncryptedSharedPreferences,
) : OnboardingStateRepository {

    private val prefKey = "onboarding_completed"

    override suspend fun getOnboardingState() = sharedPreferences.getBoolean(prefKey, false)

    override suspend fun saveOnboardingState(isCompleted: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(prefKey, isCompleted)
            apply()
        }
    }
}
