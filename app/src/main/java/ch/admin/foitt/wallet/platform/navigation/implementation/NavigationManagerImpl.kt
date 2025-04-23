package ch.admin.foitt.wallet.platform.navigation.implementation

import android.annotation.SuppressLint
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.walletcomposedestinations.appCurrentDestinationFlow
import ch.admin.foitt.walletcomposedestinations.appDestination
import ch.admin.foitt.walletcomposedestinations.destinations.Destination
import ch.admin.foitt.walletcomposedestinations.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.utils.route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

class NavigationManagerImpl @Inject constructor(
    @IoDispatcherScope private val ioDispatcherScope: CoroutineScope,
) : NavigationManager {
    private var destinationFlowJob: Job? = null
    private var backStackFlowJob: Job? = null

    private var navHost: WeakReference<NavHostController> = WeakReference(null)

    override fun setNavHost(navHost: NavHostController) {
        this.navHost = WeakReference(navHost)
        initDestinationFlow(navHost)
        initBackStackFlow(navHost)
    }

    override fun navigateTo(direction: Direction) {
        navigateTo(
            direction = direction,
            navOptionsBuilder = {},
        )
    }

    override fun navigateToAndClearCurrent(direction: Direction) {
        navigateTo(
            direction = direction,
            navOptionsBuilder = { navOptionsBuilder ->
                currentDestination?.let { crtDestination ->
                    navOptionsBuilder.popUpTo(crtDestination.route) {
                        inclusive = true
                    }
                }
            }
        )
    }

    override fun navigateToAndPopUpTo(
        direction: Direction,
        route: String,
        inclusivePop: Boolean,
    ) {
        navigateTo(
            direction = direction,
            navOptionsBuilder = { navOptionsBuilder ->
                navOptionsBuilder.popUpTo(route) {
                    inclusive = inclusivePop
                }
            }
        )
    }

    private fun initDestinationFlow(navigationHost: NavHostController) {
        destinationFlowJob?.cancel()
        destinationFlowJob = ioDispatcherScope.launch {
            navigationHost.appCurrentDestinationFlow.collect { destination ->
                _currentDestinationFlow.update { destination }
            }
        }
    }

    override val currentDestination: Destination?
        get() = navHost.get()?.currentBackStackEntry?.appDestination()

    override val previousDestination: Destination?
        get() = navHost.get()?.previousBackStackEntry?.appDestination()

    private val _currentDestinationFlow: MutableStateFlow<Destination?> = MutableStateFlow(null)
    override val currentDestinationFlow = _currentDestinationFlow.asStateFlow()

    @SuppressLint("RestrictedApi")
    private fun initBackStackFlow(navigationHost: NavHostController) {
        backStackFlowJob?.cancel()
        backStackFlowJob = ioDispatcherScope.launch {
            navigationHost.currentBackStack.collect { backStack ->
                _currentBackStackFlow.update {
                    backStack.mapNotNull { backStackEntry ->
                        when (backStackEntry.route()) {
                            is DestinationSpec<*> -> backStackEntry.appDestination()
                            is NavGraphSpec -> null
                        }
                    }
                }
            }
        }
    }

    private val _currentBackStackFlow = MutableStateFlow<List<Destination>>(listOf())
    override val currentBackStackFlow = _currentBackStackFlow.asStateFlow()

    override fun popBackStack() {
        navHost.get()?.popBackStack()
        printBackStack("pop")
    }

    override fun popBackStackTo(destination: Destination, inclusive: Boolean): Boolean {
        val backStackPopped = navHost.get()?.popBackStack(
            route = destination.route,
            inclusive = inclusive
        ) ?: false
        printBackStack("pop up to: $destination, inclusive: $inclusive")
        return backStackPopped
    }

    override fun navigateUp() {
        navHost.get()?.navigateUp()
        printBackStack("up")
    }

    private fun navigateTo(
        direction: Direction,
        navOptionsBuilder: (NavOptionsBuilder) -> Unit,
    ) {
        navHost.get()?.navigate(
            direction = direction,
            navOptionsBuilder = navOptionsBuilder,
        ) ?: Timber.w("NavAction navigate failed, navHost is null")
        printBackStack("navigate")
    }

    @SuppressLint("RestrictedApi")
    private fun printBackStack(action: String) {
        Timber.d(
            "NavAction $action, current backStack ${navHost.get()?.currentBackStack?.value?.map {
                it.route().route
            }}"
        )
    }

    override fun navigateBackToHome(popUntil: Destination) {
        val isHomeOnBackstack = popBackStackTo(HomeScreenDestination, false)
        if (!isHomeOnBackstack) {
            navigateToAndPopUpTo(
                direction = HomeScreenDestination,
                route = popUntil.route
            )
        }
    }
}
