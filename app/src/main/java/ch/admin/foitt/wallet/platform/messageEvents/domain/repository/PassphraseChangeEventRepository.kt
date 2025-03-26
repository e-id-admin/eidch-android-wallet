package ch.admin.foitt.wallet.platform.messageEvents.domain.repository

import ch.admin.foitt.wallet.platform.messageEvents.domain.model.PassphraseChangeEvent
import kotlinx.coroutines.flow.StateFlow

interface PassphraseChangeEventRepository {
    val event: StateFlow<PassphraseChangeEvent>

    fun setEvent(event: PassphraseChangeEvent)
    fun resetEvent()
}
