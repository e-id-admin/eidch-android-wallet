package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SaveEIdRequestCaseError
import com.github.michaelbull.result.Result

interface SaveEIdRequestCase {
    suspend operator fun invoke(case: EIdRequestCase): Result<Unit, SaveEIdRequestCaseError>
}
