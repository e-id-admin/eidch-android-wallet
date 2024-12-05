package ch.admin.foitt.wallet.platform.scaffold.extension

import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.hilt.navigation.compose.hiltViewModel
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.theme.LocalIsInDarkTheme
import ch.admin.foitt.walletcomposedestinations.destinations.Destination
import ch.admin.foitt.walletcomposedestinations.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.manualcomposablecalls.composable

inline fun <reified T2 : ScreenViewModel> ManualComposableCallsBuilder.screenDestination(
    destination: Destination,
    crossinline screen: @Composable (viewModel: T2) -> Unit,
) = composable(destination) {
    val viewModel: T2 = hiltViewModel()

    // Setup the top and bottom bar areas, including the navigation and status bar styling
    val currentActivity = LocalActivity.current
    val isInDarkTheme = LocalIsInDarkTheme.current
    DisposableEffect(currentActivity, viewModel, isInDarkTheme) {
        viewModel.syncScaffoldState(
            currentActivity::enableEdgeToEdge,
            isInDarkTheme
        )
        onDispose { }
    }
    screen(viewModel)
}

fun NavigationManager.navigateUpOrToRoot() {
    if (previousDestination != null) {
        navigateUp()
    } else {
        navigateToAndClearCurrent(HomeScreenDestination)
    }
}
