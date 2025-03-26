package ch.admin.foitt.wallet.platform.messageEvents.data.repository

import ch.admin.foitt.wallet.platform.messageEvents.domain.model.PassphraseChangeEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.PassphraseChangeEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class PassphraseChangeEventRepositoryImpl @Inject constructor() : PassphraseChangeEventRepository {
    private val _event = MutableStateFlow(PassphraseChangeEvent.NONE)
    override val event = _event.asStateFlow()

    override fun setEvent(event: PassphraseChangeEvent) {
        _event.value = event
    }

    override fun resetEvent() {
        _event.value = PassphraseChangeEvent.NONE
    }
}
