package ch.admin.foitt.wallet.feature.presentationRequest.domain.repository

import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestRepositoryError
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplaysAndClaims
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface PresentationRequestRepository {
    fun getPresentationCredentialListFlow(): Flow<Result<List<CredentialWithDisplays>, PresentationRequestRepositoryError>>
    fun getPresentationCredentialFlow(id: Long): Flow<Result<CredentialWithDisplaysAndClaims, PresentationRequestRepositoryError>>
}
