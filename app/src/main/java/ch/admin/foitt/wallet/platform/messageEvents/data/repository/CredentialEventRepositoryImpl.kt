package ch.admin.foitt.wallet.platform.messageEvents.data.repository

import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class CredentialEventRepositoryImpl @Inject constructor() : CredentialEventRepository {
    private val _event = MutableStateFlow(CredentialEvent.NONE)
    override val event = _event.asStateFlow()

    override fun setEvent(event: CredentialEvent) {
        _event.value = event
    }

    override fun resetEvent() {
        _event.value = CredentialEvent.NONE
    }
}
