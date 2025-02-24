package ch.admin.foitt.wallet.platform.cameraPermissionHandler.data.repository

import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.repository.CameraIntroRepository
import javax.inject.Inject

class SharedPrefsCameraIntroRepository @Inject constructor(
    private val sharedPreferences: EncryptedSharedPreferences,
) : CameraIntroRepository {
    private val prefKey = "camera_intro_was_passed"

    override suspend fun getPermissionPromptWasTriggered() = sharedPreferences.getBoolean(prefKey, false)

    override suspend fun setPermissionPromptWasTriggered(introWasPassed: Boolean) {
        sharedPreferences.edit {
            putBoolean(prefKey, introWasPassed)
        }
    }
}
