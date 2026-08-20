package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.GetTrustDomainFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessProtectedIssuanceTrustListStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementRepositoryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateProtectedIssuanceTrustListStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toProcessProtectedIssuanceTrustListStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessProtectedIssuanceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateProtectedIssuanceTrustListStatement
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class ProcessProtectedIssuanceTrustListStatementImpl @Inject constructor(
    private val getTrustDomainFromDid: GetTrustDomainFromDid,
    private val trustStatementRepository: TrustStatementRepository,
    private val validateProtectedIssuanceTrustListStatement: ValidateProtectedIssuanceTrustListStatement,
) : ProcessProtectedIssuanceTrustListStatement {
    override suspend fun invoke(
        issuerDid: String,
        vct: String,
    ): Result<Boolean, ProcessProtectedIssuanceTrustListStatementError> = coroutineBinding {
        val trustRegistryDomain = getTrustDomainFromDid(issuerDid)
            .mapError(GetTrustDomainFromDidError::toProcessProtectedIssuanceTrustListStatementError)
            .bind()

        val piTLSJwt = trustStatementRepository.fetchProtectedIssuanceTrustListStatement(trustRegistryDomain)
            .mapError(TrustStatementRepositoryError::toProcessProtectedIssuanceTrustListStatementError)
            .bind()

        val protectedIssuanceTrustListStatement = validateProtectedIssuanceTrustListStatement(
            trustStatement = piTLSJwt
        ).mapError(ValidateProtectedIssuanceTrustListStatementError::toProcessProtectedIssuanceTrustListStatementError)
            .bind()

        protectedIssuanceTrustListStatement.vctValues.contains(vct)
    }
}
