package ch.admin.foitt.wallet.feature.home.data.repository

import ch.admin.foitt.wallet.feature.home.domain.model.HomeRepositoryError
import ch.admin.foitt.wallet.feature.home.domain.model.toHomeRepositoryError
import ch.admin.foitt.wallet.feature.home.domain.repository.HomeRepository
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithIssuerAndDisplays
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import com.github.michaelbull.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class HomeRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
) : HomeRepository {

    override fun getHomeData(): Flow<Result<List<CredentialWithIssuerAndDisplays>, HomeRepositoryError>> =
        credentialWithIssuerAndDisplaysDaoFlow.flatMapLatest { dao ->
            dao?.getCredentialsWithIssuerAndDisplaysFlow()
                ?.catchAndMap(Throwable::toHomeRepositoryError) ?: emptyFlow()
        }

    private val credentialWithIssuerAndDisplaysDaoFlow = daoProvider.credentialWithIssuerAndDisplaysDaoFlow
}
