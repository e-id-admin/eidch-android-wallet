package ch.admin.foitt.wallet.platform.login.domain.model

import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricAuthenticationError
import ch.admin.foitt.wallet.platform.biometrics.domain.model.BiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.model.GetBiometricsCipherError
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.LoadAndDecryptPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError

interface LoginError {
    data object InvalidPassphrase : LoginWithPassphraseError, LoginWithBiometricsError
    data object BiometricsChanged : LoginWithBiometricsError
    data object Cancelled : LoginWithBiometricsError
    data object BiometricsLocked : LoginWithBiometricsError
    data class Unexpected(val cause: Throwable?) : LoginWithPassphraseError, LoginWithBiometricsError
}

sealed interface LoginWithPassphraseError : LoginError
sealed interface LoginWithBiometricsError : LoginError

//region Error to Error mappings

fun OpenDatabaseError.toLoginWithPassphraseError(): LoginWithPassphraseError = when (this) {
    is DatabaseError.WrongPassphrase -> LoginError.InvalidPassphrase
    DatabaseError.AlreadyOpen,
    is DatabaseError.SetupFailed -> LoginError.Unexpected(null)
}

fun OpenDatabaseError.toLoginWithBiometricsError(): LoginWithBiometricsError = when (this) {
    is DatabaseError.WrongPassphrase -> LoginError.InvalidPassphrase
    DatabaseError.AlreadyOpen,
    is DatabaseError.SetupFailed -> LoginError.Unexpected(null)
}

fun PepperPassphraseError.toLoginWithPassphraseError(): LoginWithPassphraseError = when (this) {
    is PepperPassphraseError.Unexpected -> LoginError.Unexpected(this.throwable)
}

fun BiometricAuthenticationError.toLoginWithBiometricsError(): LoginWithBiometricsError = when (this) {
    BiometricAuthenticationError.PromptCancelled -> LoginError.Cancelled
    BiometricAuthenticationError.PromptLocked -> LoginError.BiometricsLocked
    is BiometricAuthenticationError.PromptFailure -> LoginError.Unexpected(this.throwable)
    is BiometricAuthenticationError.Unexpected -> LoginError.Unexpected(this.throwable)
}

fun HashDataError.toLoginWithPassphraseError(): LoginWithPassphraseError = when (this) {
    is HashDataError.Unexpected -> LoginError.Unexpected(this.throwable)
}

fun GetBiometricsCipherError.toLoginWithBiometricsError(): LoginWithBiometricsError = when (this) {
    BiometricsError.InvalidatedKey -> LoginError.BiometricsChanged
    is BiometricsError.Unexpected -> LoginError.Unexpected(cause)
}

fun LoadAndDecryptPassphraseError.toLoginWithBiometricsError(): LoginWithBiometricsError = when (this) {
    is LoadAndDecryptPassphraseError.Unexpected -> LoginError.Unexpected(this.throwable)
}
//endregion
