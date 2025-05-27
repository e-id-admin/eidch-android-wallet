package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import kotlinx.coroutines.flow.StateFlow

fun interface GetCurrentSIdCaseId {
    operator fun invoke(): StateFlow<String?>
}
