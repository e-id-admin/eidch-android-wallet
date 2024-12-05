package ch.admin.foitt.wallet.platform.database.domain.usecase

import ch.admin.foitt.wallet.platform.database.domain.model.ChangeDatabasePassphraseError
import com.github.michaelbull.result.Result

fun interface ChangeDatabasePassphrase {
    suspend operator fun invoke(newPassphrase: ByteArray): Result<Unit, ChangeDatabasePassphraseError>
}
