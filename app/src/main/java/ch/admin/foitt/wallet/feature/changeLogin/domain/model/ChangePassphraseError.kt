package ch.admin.foitt.wallet.feature.changeLogin.domain.model

import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataError
import ch.admin.foitt.wallet.platform.database.domain.model.ChangeDatabasePassphraseError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError

sealed interface ChangePassphraseError {
    val throwable: Throwable?
    data class Unexpected(override val throwable: Throwable) : ChangePassphraseError
}

//region Error to Error mappings
fun HashDataError.toChangePassphraseError(): ChangePassphraseError = when (this) {
    is HashDataError.Unexpected -> ChangePassphraseError.Unexpected(this.throwable)
}

fun PepperPassphraseError.toChangePassphraseError(): ChangePassphraseError = when (this) {
    is PepperPassphraseError.Unexpected -> ChangePassphraseError.Unexpected(this.throwable)
}

fun ChangeDatabasePassphraseError.toChangePassphraseError(): ChangePassphraseError = when (this) {
    is DatabaseError.ReKeyFailed -> ChangePassphraseError.Unexpected(this.throwable)
}

//endregion
