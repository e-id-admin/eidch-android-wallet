package ch.admin.foitt.wallet.platform.scaffold.domain.repository

import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import kotlinx.coroutines.flow.StateFlow

interface TopBarStateRepository {
    val state: StateFlow<TopBarState>

    fun setState(state: TopBarState)
}
