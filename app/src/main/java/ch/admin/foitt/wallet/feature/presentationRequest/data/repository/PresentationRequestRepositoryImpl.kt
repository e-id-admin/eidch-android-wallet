package ch.admin.foitt.wallet.feature.presentationRequest.data.repository

import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestRepositoryError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.toPresentationRequestRepositoryError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.repository.PresentationRequestRepository
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import com.github.michaelbull.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class PresentationRequestRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
) : PresentationRequestRepository {
    override fun getPresentationCredentialListFlow(): Flow<Result<List<CredentialWithDisplays>, PresentationRequestRepositoryError>> =
        credentialWithDisplaysDaoFlow.flatMapLatest { dao ->
            dao?.getCredentialsWithDisplaysFlow()
                ?.catchAndMap(Throwable::toPresentationRequestRepositoryError) ?: emptyFlow()
        }

    override fun getPresentationCredentialFlow(
        id: Long
    ): Flow<Result<CredentialWithDisplaysAndClaims, PresentationRequestRepositoryError>> =
        credentialWithDisplaysAndClaimsDaoFlow.flatMapLatest { dao ->
            dao?.getCredentialWithDisplaysAndClaimsFlowById(id)
                ?.catchAndMap(Throwable::toPresentationRequestRepositoryError) ?: emptyFlow()
        }

    private val credentialWithDisplaysDaoFlow = daoProvider.credentialWithDisplaysDaoFlow
    private val credentialWithDisplaysAndClaimsDaoFlow = daoProvider.credentialWithDisplaysAndClaimsDaoFlow
}
