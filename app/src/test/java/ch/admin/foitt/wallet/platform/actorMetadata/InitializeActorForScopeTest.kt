package ch.admin.foitt.wallet.platform.actorMetadata

import ch.admin.foitt.wallet.platform.actorMetadata.di.ActorRepositoryEntryPoint
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.repository.ActorRepository
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.InitializeActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.InitializeActorForScopeImpl
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InitializeActorForScopeTest {

    @MockK
    private lateinit var mockDestinationScopedComponentManager: DestinationScopedComponentManager

    @MockK
    private lateinit var mockActorRepository: ActorRepository

    @MockK
    private lateinit var mockActorRepositoryEntryPoint: ActorRepositoryEntryPoint

    private lateinit var useCase: InitializeActorForScope

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = InitializeActorForScopeImpl(
            destinationScopedComponentManager = mockDestinationScopedComponentManager,
        )

        coEvery {
            mockDestinationScopedComponentManager.getEntryPoint(ActorRepositoryEntryPoint::class.java, componentScope = any())
        } returns mockActorRepositoryEntryPoint

        coEvery {
            mockActorRepositoryEntryPoint.actorRepository()
        } returns mockActorRepository

        coEvery {
            mockActorRepository.setActor(any())
        } just runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `InitializeActorForScope is following specific steps`(): Unit = runTest {
        useCase.invoke(
            actorDisplayData = mockActorDisplayData01,
            componentScope = ComponentScope.CredentialIssuer,
        )

        coVerifyOrder {
            mockDestinationScopedComponentManager.getEntryPoint(ActorRepositoryEntryPoint::class.java, any())
            mockActorRepositoryEntryPoint.actorRepository()
            mockActorRepository.setActor(
                actor = mockActorDisplayData01,
            )
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
