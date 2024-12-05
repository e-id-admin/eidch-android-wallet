package ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.model.AuthenticateWithPassphraseError
import com.github.michaelbull.result.Result

fun interface AuthenticateWithPassphrase {
    @CheckResult
    suspend operator fun invoke(passphrase: String): Result<Unit, AuthenticateWithPassphraseError>
}
