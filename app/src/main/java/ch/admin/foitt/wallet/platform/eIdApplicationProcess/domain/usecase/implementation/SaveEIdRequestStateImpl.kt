package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toSaveEIdRequestStateError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SaveEIdRequestState
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class SaveEIdRequestStateImpl @Inject constructor(
    private val saveEIdRequestStateRepository: EIdRequestStateRepository,
) : SaveEIdRequestState {
    override suspend fun invoke(state: EIdRequestState) = saveEIdRequestStateRepository.saveEIdRequestState(state)
        .mapError(EIdRequestStateRepositoryError::toSaveEIdRequestStateError)
}
