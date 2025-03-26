package ch.admin.foitt.wallet.feature.mrzScan.domain.usecase

import ch.admin.foitt.wallet.feature.mrzScan.domain.model.SaveEIdRequestCaseError
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import com.github.michaelbull.result.Result

interface SaveEIdRequestCase {
    suspend operator fun invoke(case: EIdRequestCase): Result<Unit, SaveEIdRequestCaseError>
}
