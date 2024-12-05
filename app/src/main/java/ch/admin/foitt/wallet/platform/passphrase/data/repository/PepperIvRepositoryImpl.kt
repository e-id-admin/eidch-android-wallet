package ch.admin.foitt.wallet.platform.passphrase.data.repository

import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.platform.passphrase.data.repository.SharedPreferencesByteArrayRepository.PrefKey
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.ByteArrayRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PepperIvRepository
import javax.inject.Inject

class PepperIvRepositoryImpl @Inject constructor(
    sharedPreferences: EncryptedSharedPreferences,
) : PepperIvRepository,
    ByteArrayRepository by SharedPreferencesByteArrayRepository(
        prefKey = PrefKey.WalletPassphrasePepperIv,
        sharedPreferences = sharedPreferences,
    )
