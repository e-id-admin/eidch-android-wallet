package ch.admin.foitt.wallet.feature.home.domain.usecase

import ch.admin.foitt.wallet.feature.home.domain.model.DeleteEIdRequestCaseError
import com.github.michaelbull.result.Result

interface DeleteEIdRequestCase {
    suspend operator fun invoke(caseId: String): Result<Unit, DeleteEIdRequestCaseError>
}
