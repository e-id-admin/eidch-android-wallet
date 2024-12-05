package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialIssuerDisplayRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toCredentialIssuerDisplayRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialIssuerDisplayRepo
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import com.github.michaelbull.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class CredentialIssuerDisplayRepoImpl @Inject constructor(
    daoProvider: DaoProvider,
) : CredentialIssuerDisplayRepo {
    override fun getIssuerDisplays(
        credentialId: Long
    ): Flow<Result<List<CredentialIssuerDisplay>, CredentialIssuerDisplayRepositoryError>> =
        credentialIssuerDisplayDaoFlow.flatMapLatest { dao ->
            dao?.getCredentialIssuerDisplaysById(credentialId)
                ?.catchAndMap(Throwable::toCredentialIssuerDisplayRepositoryError) ?: emptyFlow()
        }

    private val credentialIssuerDisplayDaoFlow = daoProvider.credentialIssuerDisplayDaoFlow
}
