package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toCredentialWithDisplaysRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialWithDisplaysRepository
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import com.github.michaelbull.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class CredentialWithDisplaysRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
) : CredentialWithDisplaysRepository {

    override fun getCredentialsWithDisplays(): Flow<Result<List<CredentialWithDisplays>, CredentialWithDisplaysRepositoryError>> =
        credentialWithDisplaysDaoFlow.flatMapLatest { dao ->
            dao?.getCredentialsWithDisplaysFlow()
                ?.catchAndMap { throwable ->
                    throwable.toCredentialWithDisplaysRepositoryError("Error when trying to get CredentialWithDisplays")
                } ?: emptyFlow()
        }

    private val credentialWithDisplaysDaoFlow = daoProvider.credentialWithDisplaysDaoFlow
}
