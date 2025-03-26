package ch.admin.foitt.wallet.platform.messageEvents.data.repository

import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialOfferEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialOfferEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class CredentialOfferEventRepositoryImpl @Inject constructor() : CredentialOfferEventRepository {
    private val _event = MutableStateFlow(CredentialOfferEvent.NONE)
    override val event = _event.asStateFlow()

    override fun setEvent(event: CredentialOfferEvent) {
        _event.value = event
    }

    override fun resetEvent() {
        _event.value = CredentialOfferEvent.NONE
    }
}
