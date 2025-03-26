package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.lifecycle.SavedStateHandle
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.asDayFullMonthYear
import ch.admin.foitt.walletcomposedestinations.destinations.EIdQueueScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EIdQueueViewModel @Inject constructor(
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    savedStateHandle: SavedStateHandle,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.Empty
    override val fullscreenState = FullscreenState.Insets

    private val navArgs = EIdQueueScreenDestination.argsFrom(savedStateHandle)
    private val rawDeadline = navArgs.rawDeadline

    private val parsedDate = if (rawDeadline != null) ZonedDateTime.parse(rawDeadline, DateTimeFormatter.ISO_ZONED_DATE_TIME) else null
    private val deadlineText = parsedDate?.asDayFullMonthYear() ?: ""
    private val _deadline = MutableStateFlow(deadlineText)
    val deadline = _deadline.asStateFlow()

    fun onNext() = navManager.navigateBackToHome(EIdQueueScreenDestination)
}
