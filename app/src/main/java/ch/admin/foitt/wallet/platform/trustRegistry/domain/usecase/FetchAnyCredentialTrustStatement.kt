package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.FetchAnyCredentialTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import com.github.michaelbull.result.Result

fun interface FetchAnyCredentialTrustStatement {
    suspend operator fun invoke(anyCredential: AnyCredential): Result<TrustStatement, FetchAnyCredentialTrustStatementError>
}
