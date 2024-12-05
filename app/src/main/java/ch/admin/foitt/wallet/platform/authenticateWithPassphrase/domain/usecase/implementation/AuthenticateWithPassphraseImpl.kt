package ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.model.AuthenticateWithPassphraseError
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.model.toAuthenticateWithPassphraseError
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.usecase.AuthenticateWithPassphrase
import ch.admin.foitt.wallet.platform.database.domain.usecase.CheckDatabasePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.scaffold.domain.model.ErrorDialogState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetErrorDialogState
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import timber.log.Timber
import javax.inject.Inject

class AuthenticateWithPassphraseImpl @Inject constructor(
    private val hashPassphrase: HashPassphrase,
    private val pepperPassphrase: PepperPassphrase,
    private val checkDatabasePassphrase: CheckDatabasePassphrase,
    private val setErrorDialogState: SetErrorDialogState,
) : AuthenticateWithPassphrase {

    @CheckResult
    override suspend fun invoke(passphrase: String): Result<Unit, AuthenticateWithPassphraseError> = coroutineBinding {
        val pinHash = hashPassphrase(
            pin = passphrase,
            initializeSalt = false
        ).mapError { error ->
            error.toAuthenticateWithPassphraseError()
        }.bind()

        val pepperedPinHash = pepperPassphrase(
            passphrase = pinHash.hash,
            initializePepper = false,
        ).mapError { error ->
            error.toAuthenticateWithPassphraseError()
        }.bind()

        checkDatabasePassphrase(passphrase = pepperedPinHash.hash)
            .onFailure {
                Timber.d("DB encryption failed, wrong passphrase")
            }.mapError { error ->
                error.toAuthenticateWithPassphraseError()
            }.bind()
    }.onFailure { authError ->
        onAuthFailure(authError)
    }

    private fun onAuthFailure(authError: AuthenticateWithPassphraseError) {
        when (authError) {
            AuthenticateWithPassphraseError.InvalidPassphrase -> {}
            is AuthenticateWithPassphraseError.Unexpected -> {
                setErrorDialogState(
                    ErrorDialogState.Wallet(
                        errorDetails = authError.cause?.localizedMessage,
                    )
                )
                Timber.e(authError.cause)
            }
        }
    }
}
