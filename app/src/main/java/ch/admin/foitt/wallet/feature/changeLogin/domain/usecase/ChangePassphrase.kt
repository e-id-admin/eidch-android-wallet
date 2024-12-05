package ch.admin.foitt.wallet.feature.changeLogin.domain.usecase

import ch.admin.foitt.wallet.feature.changeLogin.domain.model.ChangePassphraseError
import com.github.michaelbull.result.Result

fun interface ChangePassphrase {
    suspend operator fun invoke(newPin: String): Result<Unit, ChangePassphraseError>
}
