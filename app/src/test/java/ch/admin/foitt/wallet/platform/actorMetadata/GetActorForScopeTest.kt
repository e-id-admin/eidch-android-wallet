package ch.admin.foitt.wallet.platform.actorMetadata

import ch.admin.foitt.wallet.platform.actorMetadata.di.ActorRepositoryEntryPoint
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.repository.ActorRepository
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.GetActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.GetActorForScopeImpl
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetActorForScopeTest {

    @MockK
    private lateinit var mockDestinationScopedComponentManager: DestinationScopedComponentManager

    @MockK
    private lateinit var mockActorRepository: ActorRepository

    @MockK
    private lateinit var mockActorRepositoryEntryPoint: ActorRepositoryEntryPoint

    private lateinit var useCase: GetActorForScope

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = GetActorForScopeImpl(
            destinationScopedComponentManager = mockDestinationScopedComponentManager,
        )

        coEvery {
            mockDestinationScopedComponentManager.getEntryPoint(ActorRepositoryEntryPoint::class.java, componentScope = any())
        } returns mockActorRepositoryEntryPoint

        coEvery {
            mockActorRepositoryEntryPoint.actorRepository()
        } returns mockActorRepository

        coEvery {
            mockActorRepository.actorDisplayData
        } returns MutableStateFlow(mockActorDisplayData01)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `GetActorForScope is following specific steps`(): Unit = runTest {
        val displayDataFlow = useCase.invoke(
            componentScope = ComponentScope.CredentialIssuer,
        )

        assertEquals(mockActorDisplayData01, displayDataFlow.value)

        coVerifyOrder {
            mockDestinationScopedComponentManager.getEntryPoint(ActorRepositoryEntryPoint::class.java, ComponentScope.CredentialIssuer)
            mockActorRepositoryEntryPoint.actorRepository()
            mockActorRepository.actorDisplayData
        }
    }

    //region mock data
    private val mockActorDisplayData01 = ActorDisplayData(
        name = listOf(
            ActorField(
                value = "test_name",
                locale = "de"
            )
        ),
        image = listOf(
            ActorField(
                value = "test_image",
                locale = "de"
            )
        ),
        preferredLanguage = "test_de",
        trustStatus = TrustStatus.TRUSTED,
        actorType = ActorType.ISSUER,
    )
    //endregion
}
