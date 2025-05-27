package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface EIdCurrentSIdCaseRepository {
    val caseId: StateFlow<String?>
    fun setCaseId(caseId: String)
}
