package ch.admin.foitt.wallet.platform.messageEvents.domain.repository

import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialEvent
import kotlinx.coroutines.flow.StateFlow

interface CredentialEventRepository {
    val event: StateFlow<CredentialEvent>

    fun setEvent(event: CredentialEvent)
    fun resetEvent()
}
