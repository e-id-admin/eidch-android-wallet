package ch.admin.foitt.wallet.feature.login.presentation

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.login.domain.usecase.IsDevicePinSet
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openSecuritySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class NoDevicePinSetViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    @ApplicationContext private val appContext: Context,
    private val isDevicePinSet: IsDevicePinSet,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Insets

    fun checkDevicePinSet() {
        if (isDevicePinSet()) {
            viewModelScope.launch {
                // short delay since OnResumeEventHandler will call all back stack entries and if we pop right away, we get an index out of bounds
                delay(100)
                navigationManager.popBackStack()
            }
        }
    }

    fun goToSettings() = appContext.openSecuritySettings()
}
