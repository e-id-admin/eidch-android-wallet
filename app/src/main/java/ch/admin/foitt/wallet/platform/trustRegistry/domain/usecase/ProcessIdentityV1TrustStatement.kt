package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityV1TrustStatementError
import com.github.michaelbull.result.Result

interface ProcessIdentityV1TrustStatement {
    suspend operator fun invoke(
        did: String,
    ): Result<IdentityV1TrustStatement?, ProcessIdentityV1TrustStatementError>
}
