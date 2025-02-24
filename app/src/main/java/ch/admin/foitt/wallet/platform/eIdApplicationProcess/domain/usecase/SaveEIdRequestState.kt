package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SaveEIdRequestStateError
import com.github.michaelbull.result.Result

interface SaveEIdRequestState {
    suspend operator fun invoke(state: EIdRequestState): Result<Long, SaveEIdRequestStateError>
}
