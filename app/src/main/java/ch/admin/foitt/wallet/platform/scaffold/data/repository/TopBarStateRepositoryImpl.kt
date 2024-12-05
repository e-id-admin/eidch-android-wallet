package ch.admin.foitt.wallet.platform.scaffold.data.repository

import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.repository.TopBarStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class TopBarStateRepositoryImpl @Inject constructor() : TopBarStateRepository {
    private val _state = MutableStateFlow<TopBarState>(TopBarState.None)

    override val state = _state.asStateFlow()

    override fun setState(state: TopBarState) {
        _state.value = state
    }
}
