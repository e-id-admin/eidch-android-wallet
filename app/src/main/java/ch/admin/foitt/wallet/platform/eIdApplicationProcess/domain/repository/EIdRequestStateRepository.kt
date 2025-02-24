package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestStateRepositoryError
import com.github.michaelbull.result.Result

interface EIdRequestStateRepository {
    suspend fun saveEIdRequestState(state: EIdRequestState): Result<Long, EIdRequestStateRepositoryError>
}
