package ch.admin.foitt.wallet.platform.passphrase.data.repository

import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.platform.passphrase.data.repository.SharedPreferencesByteArrayRepository.PrefKey
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.ByteArrayRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.SaltRepository
import javax.inject.Inject

class SaltRepositoryImpl @Inject constructor(
    private val sharedPreferences: EncryptedSharedPreferences,
) : SaltRepository,
    ByteArrayRepository by SharedPreferencesByteArrayRepository(
        prefKey = PrefKey.WalletPassphraseSalt,
        sharedPreferences = sharedPreferences,
    )
