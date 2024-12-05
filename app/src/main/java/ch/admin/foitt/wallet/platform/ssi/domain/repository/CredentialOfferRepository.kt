package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDetails
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.LocalizedCredentialOffer
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface CredentialOfferRepository {
    fun getCredentialOfferByIdFlow(id: Long): Flow<Result<CredentialWithDetails?, CredentialOfferRepositoryError>>
    suspend fun saveCredentialOffer(localizedCredentialOffer: LocalizedCredentialOffer): Result<Long, CredentialOfferRepositoryError>
}
