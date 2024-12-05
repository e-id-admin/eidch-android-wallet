package ch.admin.foitt.wallet.platform.passphrase.data.repository

import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.ByteArrayRepository
import ch.admin.foitt.wallet.platform.utils.base64StringToByteArray
import ch.admin.foitt.wallet.platform.utils.toBase64String

class SharedPreferencesByteArrayRepository constructor(
    private val prefKey: PrefKey,
    private val sharedPreferences: EncryptedSharedPreferences,
) : ByteArrayRepository {
    override suspend fun get(): ByteArray {
        val base64SaltString = sharedPreferences.getString(prefKey.value, null) ?: ""
        return base64SaltString.base64StringToByteArray()
    }

    override suspend fun save(data: ByteArray) {
        sharedPreferences.edit {
            putString(prefKey.value, data.toBase64String())
        }
    }

    sealed class PrefKey(val value: String) {
        object WalletPassphrasePepperIv : PrefKey("wallet_passphrase_pepper_iv")
        object WalletPassphraseSalt : PrefKey("wallet_passphrase_salt")
    }
}
