package ch.admin.foitt.wallet.feature.home.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.home.domain.model.toDeleteEIdRequestCaseError
import ch.admin.foitt.wallet.feature.home.domain.usecase.DeleteEIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class DeleteEIdRequestCaseImpl @Inject constructor(
    private val eIdRequestCaseRepository: EIdRequestCaseRepository,
) : DeleteEIdRequestCase {
    override suspend fun invoke(caseId: String) = eIdRequestCaseRepository.deleteEIdRequestCase(caseId)
        .mapError(EIdRequestCaseRepositoryError::toDeleteEIdRequestCaseError)
}
