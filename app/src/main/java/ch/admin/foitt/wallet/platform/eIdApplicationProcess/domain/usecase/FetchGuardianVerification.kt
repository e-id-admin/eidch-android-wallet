package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianVerificationError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianVerificationResponse
import com.github.michaelbull.result.Result

fun interface FetchGuardianVerification {
    suspend operator fun invoke(caseId: String): Result<GuardianVerificationResponse, GuardianVerificationError>
}
