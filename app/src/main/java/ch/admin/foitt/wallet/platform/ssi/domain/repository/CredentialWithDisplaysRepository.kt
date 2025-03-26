package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface CredentialWithDisplaysRepository {
    fun getCredentialsWithDisplays(): Flow<Result<List<CredentialWithDisplays>, CredentialWithDisplaysRepositoryError>>
}
