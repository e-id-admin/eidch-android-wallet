package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AbortSIdProcessError
import com.github.michaelbull.result.Result

interface AbortSIdProcess {
    suspend operator fun invoke(caseId: String): Result<Unit, AbortSIdProcessError>
}
