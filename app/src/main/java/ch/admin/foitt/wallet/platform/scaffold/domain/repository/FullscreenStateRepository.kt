package ch.admin.foitt.wallet.platform.scaffold.domain.repository

import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import kotlinx.coroutines.flow.StateFlow

interface FullscreenStateRepository {
    val state: StateFlow<FullscreenState>

    fun setState(state: FullscreenState)
}
