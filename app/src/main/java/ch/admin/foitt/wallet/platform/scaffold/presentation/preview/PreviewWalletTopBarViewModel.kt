package ch.admin.foitt.wallet.platform.scaffold.presentation.preview

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.implementation.NavigationManagerImpl
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.repository.TopBarStateRepository
import ch.admin.foitt.wallet.platform.scaffold.presentation.WalletTopBarViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreviewWalletTopBarViewModel(
    navigationManager: NavigationManager,
    mockTopBarStateRepo: MockTopBarStateRepo,
) : WalletTopBarViewModel(navigationManager, mockTopBarStateRepo)

class MockTopBarStateRepo(
    mockState: TopBarState,
) : TopBarStateRepository {
    private val _state = MutableStateFlow(mockState)

    override val state: StateFlow<TopBarState>
        get() = _state.asStateFlow()

    override fun setState(state: TopBarState) {
        _state.value = state
    }
}

@Composable
fun getPreviewWalletTopBarViewModel(
    @StringRes titleId: Int,
): PreviewWalletTopBarViewModel {
    val coroutineScope = rememberCoroutineScope()
    val mockViewModel = remember {
        PreviewWalletTopBarViewModel(
            navigationManager = NavigationManagerImpl(coroutineScope),
            mockTopBarStateRepo = MockTopBarStateRepo(
                mockState = TopBarState.Transparent(titleId = titleId, onUp = {}),
            )
        )
    }

    return mockViewModel
}
