package ch.admin.foitt.wallet.platform.messageEvents.domain.repository

import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialOfferEvent
import kotlinx.coroutines.flow.StateFlow

interface CredentialOfferEventRepository {
    val event: StateFlow<CredentialOfferEvent>

    fun setEvent(event: CredentialOfferEvent)
    fun resetEvent()
}
