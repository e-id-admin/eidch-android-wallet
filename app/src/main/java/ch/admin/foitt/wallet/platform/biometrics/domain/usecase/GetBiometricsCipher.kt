package ch.admin.foitt.wallet.platform.biometrics.domain.usecase

import ch.admin.foitt.wallet.platform.biometrics.domain.model.GetBiometricsCipherError
import com.github.michaelbull.result.Result
import javax.crypto.Cipher

fun interface GetBiometricsCipher {
    suspend operator fun invoke(): Result<Cipher, GetBiometricsCipherError>
}
