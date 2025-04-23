package ch.admin.foitt.wallet.platform.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavHostController
import ch.admin.foitt.walletcomposedestinations.destinations.Destination
import com.ramcosta.composedestinations.spec.Direction
import kotlinx.coroutines.flow.StateFlow

interface NavigationManager {

    val currentDestination: Destination?
    val previousDestination: Destination?
    val currentDestinationFlow: StateFlow<Destination?>
    val currentBackStackFlow: StateFlow<List<Destination>>

    fun setNavHost(navHost: NavHostController)

    @MainThread
    fun navigateTo(
        direction: Direction,
    )

    @MainThread
    fun navigateToAndClearCurrent(
        direction: Direction,
    )

    @MainThread
    fun navigateToAndPopUpTo(
        direction: Direction,
        route: String,
        inclusivePop: Boolean = true
    )

    @MainThread
    fun popBackStack()

    @MainThread
    fun popBackStackTo(
        destination: Destination,
        inclusive: Boolean,
    ): Boolean

    @MainThread
    fun navigateUp()

    fun navigateBackToHome(popUntil: Destination)
}
