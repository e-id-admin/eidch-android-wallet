package ch.admin.foitt.wallet.platform.scaffold.data.repository

import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.repository.FullscreenStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class FullscreenStateRepositoryImpl @Inject constructor() : FullscreenStateRepository {
    private val _state = MutableStateFlow<FullscreenState>(FullscreenState.Insets)

    override val state = _state.asStateFlow()

    override fun setState(state: FullscreenState) {
        _state.value = state
    }
}
