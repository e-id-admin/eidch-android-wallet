package ch.admin.foitt.wallet.platform.navigation

import ch.admin.foitt.wallet.platform.actorMetadata.di.ActorRepositoryEntryPoint
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.navigation.implementation.DestinationScopedComponentManagerImpl
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.Destination
import ch.admin.foitt.walletcomposedestinations.destinations.HomeScreenDestination
import dagger.hilt.EntryPoints
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class DestinationScopedComponentManagerTest {
    @MockK
    private lateinit var mockNavManager: NavigationManager

    @MockK
    private lateinit var mockDestinationsComponentBuilder: DestinationsComponentBuilder

    @MockK
    private lateinit var mockDestinationScopedComponent: DestinationScopedComponent

    @MockK
    private lateinit var mockActorRepositoryEntryPoint: ActorRepositoryEntryPoint

    private lateinit var backStackFlow: MutableStateFlow<List<Destination>>
    private lateinit var manager: DestinationScopedComponentManagerImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(EntryPoints::class)

        backStackFlow = MutableStateFlow(
            listOf(HomeScreenDestination, ComponentScope.CredentialIssuer.destinations.first())
        )

        coEvery { EntryPoints.get<Any>(any(), any()) } returns mockActorRepositoryEntryPoint

        coEvery { mockDestinationsComponentBuilder.setScope(any()) } returns mockDestinationsComponentBuilder
        coEvery { mockDestinationsComponentBuilder.build() } returns mockDestinationScopedComponent

        coEvery { mockNavManager.currentDestination } returns CredentialOfferScreenDestination
        coEvery { mockNavManager.currentBackStackFlow } returns backStackFlow

        runTest {
            manager = spyk(
                DestinationScopedComponentManagerImpl(
                    componentBuilder = mockDestinationsComponentBuilder,
                    navManager = mockNavManager,
                    ioDispatcherScope = backgroundScope,
                ),
                recordPrivateCalls = true,
            )
        }
    }

    @AfterEach
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `Getting an EntryPoint with a correspond registered scope succeeds`(): Unit = runTest {
        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.CredentialIssuer,
        )
    }

    @Test
    fun `Getting an EntryPoint without a corresponding scope throw an exception`(): Unit = runTest {
        coEvery { mockNavManager.currentDestination } returns HomeScreenDestination

        assertThrows<IllegalArgumentException> {
            manager.getEntryPoint(
                ActorRepositoryEntryPoint::class.java,
                ComponentScope.CredentialIssuer,
            )
        }
    }

    @Test
    fun `Getting an EntryPoint for a not yet cached scope generate a new component`(): Unit = runTest {
        val scopedComponents = manager.getScopedComponentsField()

        coEvery { mockNavManager.currentDestination } returns ComponentScope.CredentialIssuer.destinations.first()
        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.CredentialIssuer,
        )

        assertEquals(1, scopedComponents.size)
        assertEquals(ComponentScope.CredentialIssuer, scopedComponents.entries.elementAt(0).key)

        coEvery { mockNavManager.currentDestination } returns ComponentScope.Verifier.destinations.first()
        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.Verifier,
        )

        assertEquals(2, scopedComponents.size)
        assertEquals(ComponentScope.Verifier, scopedComponents.entries.elementAt(1).key)
    }

    @Test
    fun `Getting an EntryPoint for an already cached scope reuse an existing component`(): Unit = runTest {
        val scopedComponents = manager.getScopedComponentsField()

        coEvery { mockNavManager.currentDestination } returns ComponentScope.CredentialIssuer.destinations.elementAt(0)
        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.CredentialIssuer,
        )

        assertEquals(1, scopedComponents.size)
        assertEquals(ComponentScope.CredentialIssuer, scopedComponents.entries.elementAt(0).key)

        coEvery { mockNavManager.currentDestination } returns ComponentScope.CredentialIssuer.destinations.elementAt(1)
        manager.getEntryPoint(
            ActorRepositoryEntryPoint::class.java,
            ComponentScope.CredentialIssuer,
        )
        assertEquals(1, scopedComponents.size)
    }

    @Suppress("UNCHECKED_CAST")
    private fun DestinationScopedComponentManagerImpl.getScopedComponentsField(): MutableMap<ComponentScope, DestinationScopedComponent> {
        val field = DestinationScopedComponentManagerImpl::class.memberProperties
            .first { member ->
                member.name == "scopedComponents"
            }.apply { isAccessible = true }
            .getter.call(this)
        return field as MutableMap<ComponentScope, DestinationScopedComponent>
    }
}
