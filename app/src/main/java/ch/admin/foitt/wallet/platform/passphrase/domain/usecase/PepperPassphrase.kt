package ch.admin.foitt.wallet.platform.passphrase.domain.usecase

import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperedData
import com.github.michaelbull.result.Result

fun interface PepperPassphrase {
    suspend operator fun invoke(
        passphrase: ByteArray,
        initializePepper: Boolean,
    ): Result<PepperedData, PepperPassphraseError>
}
