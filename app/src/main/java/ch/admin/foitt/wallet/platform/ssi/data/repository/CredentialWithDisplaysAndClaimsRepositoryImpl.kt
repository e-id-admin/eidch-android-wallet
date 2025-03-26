package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysAndClaimsRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toCredentialWithDisplaysAndClaimsRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialWithDisplaysAndClaimsRepository
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import com.github.michaelbull.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class CredentialWithDisplaysAndClaimsRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
) : CredentialWithDisplaysAndClaimsRepository {

    override fun getCredentialWithDisplaysAndClaimsFlowById(
        credentialId: Long
    ): Flow<Result<CredentialWithDisplaysAndClaims, CredentialWithDisplaysAndClaimsRepositoryError>> =
        credentialWithDisplaysAndClaimsDaoFlow.flatMapLatest { dao ->
            dao?.getCredentialWithDisplaysAndClaimsFlowById(credentialId)
                ?.catchAndMap { throwable ->
                    throwable.toCredentialWithDisplaysAndClaimsRepositoryError("Error to get CredentialWithDisplaysAndClaims")
                } ?: emptyFlow()
        }

    override fun getNullableCredentialWithDisplaysAndClaimsFlowById(
        credentialId: Long
    ): Flow<Result<CredentialWithDisplaysAndClaims?, CredentialWithDisplaysAndClaimsRepositoryError>> =
        credentialWithDisplaysAndClaimsDaoFlow.flatMapLatest { dao ->
            dao?.getNullableCredentialWithDisplaysAndClaimsFlowById(credentialId)
                ?.catchAndMap { throwable ->
                    throwable.toCredentialWithDisplaysAndClaimsRepositoryError("Error to get NullableCredentialWithDisplaysAndClaims")
                } ?: emptyFlow()
        }

    private val credentialWithDisplaysAndClaimsDaoFlow = daoProvider.credentialWithDisplaysAndClaimsDaoFlow
}
