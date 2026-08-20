package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IsTrustedDidError
import com.github.michaelbull.result.Result

interface IsTrustedDid {
    suspend operator fun invoke(
        keyId: String,
        trustStatementType: String,
    ): Result<Unit, IsTrustedDidError>
}
