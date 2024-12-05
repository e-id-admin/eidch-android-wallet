package ch.admin.foitt.wallet.platform.login.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.login.domain.model.LoginWithPassphraseError
import com.github.michaelbull.result.Result

fun interface LoginWithPassphrase {
    @CheckResult
    suspend operator fun invoke(passphrase: String): Result<Unit, LoginWithPassphraseError>
}
