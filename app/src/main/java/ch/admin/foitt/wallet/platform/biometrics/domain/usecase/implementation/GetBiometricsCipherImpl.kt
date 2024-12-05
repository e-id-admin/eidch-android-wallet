package ch.admin.foitt.wallet.platform.biometrics.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.biometrics.domain.model.GetBiometricsCipherError
import ch.admin.foitt.wallet.platform.biometrics.domain.model.toGetBiometricsCipherError
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.GetBiometricsCipher
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.GetCipherForDecryptionError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetCipherForDecryption
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseStorageKeyConfig
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PassphraseRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import javax.crypto.Cipher
import javax.inject.Inject

class GetBiometricsCipherImpl @Inject constructor(
    private val getCipherForDecryption: GetCipherForDecryption,
    private val passphraseStorageKeyConfig: PassphraseStorageKeyConfig,
    private val passphraseRepository: PassphraseRepository,
) : GetBiometricsCipher {
    override suspend fun invoke(): Result<Cipher, GetBiometricsCipherError> = getCipherForDecryption(
        passphraseStorageKeyConfig,
        passphraseRepository.getPassphrase().initializationVector,
    ).mapError(GetCipherForDecryptionError::toGetBiometricsCipherError)
}
