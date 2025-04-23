package ch.admin.foitt.wallet.platform.navigation.implementation

import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponent
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.DestinationsComponentBuilder
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.walletcomposedestinations.destinations.Destination
import dagger.hilt.EntryPoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

internal class DestinationScopedComponentManagerImpl @Inject constructor(
    private val componentBuilder: DestinationsComponentBuilder,
    private val navManager: NavigationManager,
    @IoDispatcherScope private val ioDispatcherScope: CoroutineScope,
) : DestinationScopedComponentManager {
    private val backStackFlow: StateFlow<List<Destination>> = navManager.currentBackStackFlow

    private val scopedComponents: MutableMap<ComponentScope, DestinationScopedComponent> = mutableMapOf()

    override fun <T> getEntryPoint(entryPointClass: Class<T>, componentScope: ComponentScope): T {
        // Runtime exception in case of coding error
        val currentDestination = navManager.currentDestination
        require(currentDestination in componentScope.destinations) {
            "the current destination is not in the scope"
        }

        val component = scopedComponents.getOrPut(componentScope) {
            componentBuilder.setScope(componentScope).build()
        }

        val entryPoint = EntryPoints.get(component, entryPointClass)
        Timber.d(
            "Component Entry Point requested:\n" +
                "EntryClass: $entryPointClass,\n" +
                "Scoped components: ${scopedComponents.entries}"
        )
        return entryPoint
    }

    init {
        ioDispatcherScope.launch {
            backStackFlow.collect { backStack ->
                updateComponents(backStack)
            }
        }
    }

    private fun updateComponents(backStack: List<Destination>) {
        Timber.d("Components update triggered")
        if (backStack.isNotEmpty()) {
            scopedComponents.keys.retainAll { scope ->
                scope.destinations.any { scopeDestination -> scopeDestination in backStack }
            }
            Timber.d(
                "Components updated:\n" +
                    "Backstack: $backStack,\n" +
                    "Scoped components: ${scopedComponents.entries}"
            )
        }
    }
}
