package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface EidApplicationProcessRepository {
    val hasLegalGuardian: StateFlow<Boolean>
    fun setHasLegalGuardian(hasLegalGuardian: Boolean)
}
