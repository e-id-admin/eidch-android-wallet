package ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdCurrentSIdCaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class EIdCurrentSIdCaseRepositoryImpl @Inject constructor() : EIdCurrentSIdCaseRepository {
    private val _caseId = MutableStateFlow<String?>(null)
    override val caseId = _caseId.asStateFlow()

    override fun setCaseId(caseId: String) {
        _caseId.value = caseId
    }
}
