package ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.GetOrCreateSecretKeyError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreKeyConfig
import com.github.michaelbull.result.Result
import javax.crypto.SecretKey

internal fun interface GetOrCreateSecretKey {
    @CheckResult
    operator fun invoke(keystoreKeyConfig: KeystoreKeyConfig): Result<SecretKey, GetOrCreateSecretKeyError>
}
