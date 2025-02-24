package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toSaveEIdRequestCaseError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SaveEIdRequestCase
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class SaveEIdRequestCaseImpl @Inject constructor(
    private val eIdRequestCaseRepository: EIdRequestCaseRepository,
) : SaveEIdRequestCase {
    override suspend fun invoke(case: EIdRequestCase) = eIdRequestCaseRepository.saveEIdRequestCase(case)
        .mapError(EIdRequestCaseRepositoryError::toSaveEIdRequestCaseError)
}
