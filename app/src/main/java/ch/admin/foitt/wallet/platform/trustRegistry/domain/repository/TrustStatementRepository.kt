package ch.admin.foitt.wallet.platform.trustRegistry.domain.repository

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementRepositoryError
import com.github.michaelbull.result.Result
import java.net.URL

interface TrustStatementRepository {
    suspend fun fetchTrustStatements(url: URL): Result<List<String>, TrustStatementRepositoryError>
    suspend fun fetchProtectedIssuanceTrustListStatement(trustRegistryDomain: String): Result<Jwt, TrustStatementRepositoryError>
    suspend fun fetchNonComplianceTrustListStatement(trustRegistryDomain: String): Result<Jwt, TrustStatementRepositoryError>
}
