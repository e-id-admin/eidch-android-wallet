package ch.admin.foitt.wallet.feature.login.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.feature.login.domain.usecase.IsDeviceSecureLockScreenConfigured
import ch.admin.foitt.wallet.feature.login.domain.usecase.LockTrigger
import ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.model.AppLifecycleState
import ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.usecase.GetAppLifecycleState
import ch.admin.foitt.wallet.platform.database.domain.usecase.CloseAppDatabase
import ch.admin.foitt.wallet.platform.database.domain.usecase.IsAppDatabaseOpen
import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.NavigationAction
import ch.admin.foitt.wallet.platform.navigation.utils.blackListedDestinationsLockScreen
import ch.admin.foitt.walletcomposedestinations.destinations.Destination
import ch.admin.foitt.walletcomposedestinations.destinations.LockScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.UnsecuredDeviceScreenDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

internal class LockTriggerImpl @Inject constructor(
    private val navManager: NavigationManager,
    private val closeAppDatabase: CloseAppDatabase,
    private val getAppLifecycleState: GetAppLifecycleState,
    private val isAppDatabaseOpen: IsAppDatabaseOpen,
    private val isDeviceSecureLockScreenConfigured: IsDeviceSecureLockScreenConfigured,
    @IoDispatcherScope private val ioDispatcherScope: CoroutineScope,
) : LockTrigger {
    @CheckResult
    override suspend fun invoke(): StateFlow<NavigationAction> = combine(
        getAppLifecycleState(),
        navManager.currentDestinationFlow,
    ) { appLifecycleState, currentDestination ->
        currentDestination ?: return@combine NavigationAction { }

        when {
            !isDeviceSecureLockScreenConfigured() -> {
                closeAppDatabase()
                navigateToNoDevicePinSet()
            }
            isScreenBlackListed(currentDestination) -> {
                closeAppDatabase()
                NavigationAction {}
            }
            appLifecycleState is AppLifecycleState.Foreground && isAppDatabaseOpen() -> NavigationAction {}
            else -> {
                // The app is in background, or is in foreground in an inconsistent state.
                closeAppDatabase()
                navigateToLockScreen()
            }
        }
    }.stateIn(
        scope = ioDispatcherScope,
    )

    private fun navigateToLockScreen() = NavigationAction {
        Timber.d("LockTrigger: lock navigation triggered")
        navManager.navigateTo(
            LockScreenDestination
        )
    }

    private fun navigateToNoDevicePinSet() = NavigationAction {
        if (navManager.currentDestination != UnsecuredDeviceScreenDestination) {
            navManager.navigateTo(UnsecuredDeviceScreenDestination)
        }
    }

    private fun isScreenBlackListed(destination: Destination): Boolean {
        return blackListedDestinationsLockScreen.contains(destination)
    }
}
