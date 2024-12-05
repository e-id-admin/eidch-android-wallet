package ch.admin.foitt.wallet.platform.appSetupState.data.repository

import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.FirstCredentialAddedRepository
import javax.inject.Inject

class FirstCredentialAddedRepositoryImpl @Inject constructor(
    private val sharedPreferences: EncryptedSharedPreferences,
) : FirstCredentialAddedRepository {
    private val prefKey = "first_credential_added"

    override suspend fun getIsAdded() = sharedPreferences.getBoolean(prefKey, false)

    override suspend fun setIsAdded() = sharedPreferences.edit {
        putBoolean(prefKey, true)
    }
}
