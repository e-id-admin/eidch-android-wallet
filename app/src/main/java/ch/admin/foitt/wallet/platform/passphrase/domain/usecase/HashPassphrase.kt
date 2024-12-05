package ch.admin.foitt.wallet.platform.passphrase.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataError
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import com.github.michaelbull.result.Result

fun interface HashPassphrase {
    @CheckResult
    suspend operator fun invoke(
        pin: String,
        initializeSalt: Boolean,
    ): Result<HashedData, HashDataError>
}
