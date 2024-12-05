package ch.admin.foitt.wallet.feature.home.domain.repository

import ch.admin.foitt.wallet.feature.home.domain.model.HomeRepositoryError
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithIssuerAndDisplays
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getHomeData(): Flow<Result<List<CredentialWithIssuerAndDisplays>, HomeRepositoryError>>
}
