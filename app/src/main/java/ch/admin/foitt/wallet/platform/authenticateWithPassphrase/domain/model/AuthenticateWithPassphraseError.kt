package ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.model

import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError

sealed interface AuthenticateWithPassphraseError {
    object InvalidPassphrase : AuthenticateWithPassphraseError
    data class Unexpected(val cause: Throwable?) : AuthenticateWithPassphraseError
}

//region Error to Error mappings

fun OpenDatabaseError.toAuthenticateWithPassphraseError(): AuthenticateWithPassphraseError = when (this) {
    is DatabaseError.WrongPassphrase -> AuthenticateWithPassphraseError.InvalidPassphrase
    DatabaseError.AlreadyOpen,
    is DatabaseError.SetupFailed -> AuthenticateWithPassphraseError.Unexpected(null)
}

fun PepperPassphraseError.toAuthenticateWithPassphraseError(): AuthenticateWithPassphraseError = when (this) {
    is PepperPassphraseError.Unexpected -> AuthenticateWithPassphraseError.Unexpected(this.throwable)
}

fun HashDataError.toAuthenticateWithPassphraseError(): AuthenticateWithPassphraseError = when (this) {
    is HashDataError.Unexpected -> AuthenticateWithPassphraseError.Unexpected(this.throwable)
}
//endregion
