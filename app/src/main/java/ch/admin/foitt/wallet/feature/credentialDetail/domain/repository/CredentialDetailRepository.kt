package ch.admin.foitt.wallet.feature.credentialDetail.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDetails
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface CredentialDetailRepository {
    fun getCredentialDetailByIdFlow(id: Long): Flow<Result<CredentialWithDetails?, CredentialRepositoryError>>
}
