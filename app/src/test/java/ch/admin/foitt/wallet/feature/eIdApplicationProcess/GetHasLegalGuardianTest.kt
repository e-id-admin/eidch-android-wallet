package ch.admin.foitt.wallet.feature.eIdApplicationProcess

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.di.EidApplicationRepositoryEntryPoint
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EidApplicationProcessRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.GetHasLegalGuardianImpl
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class GetHasLegalGuardianTest {

    @MockK
    private lateinit var mockDestinationScopedComponentManager: DestinationScopedComponentManager

    @MockK
    private lateinit var mockEidApplicationProcessRepository: EidApplicationProcessRepository

    @MockK
    private lateinit var mockEidApplicationProcessEntryPoint: EidApplicationRepositoryEntryPoint

    private lateinit var mockFlow: MutableStateFlow<Boolean>

    private lateinit var useCase: GetHasLegalGuardianImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockFlow = MutableStateFlow(false)

        useCase = GetHasLegalGuardianImpl(
            destinationScopedComponentManager = mockDestinationScopedComponentManager,
        )

        coEvery {
            mockDestinationScopedComponentManager.getEntryPoint(EidApplicationRepositoryEntryPoint::class.java, componentScope = any())
        } returns mockEidApplicationProcessEntryPoint

        coEvery {
            mockEidApplicationProcessEntryPoint.eidApplicationProcessRepository()
        } returns mockEidApplicationProcessRepository

        coEvery {
            mockEidApplicationProcessRepository.hasLegalGuardian
        } returns mockFlow
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @ValueSource(
        booleans = [true, false]
    )
    fun `HasLegalGuardian is getting the proper value from the repository`(hasLegalGuardian: Boolean): Unit = runTest {
        mockFlow.value = hasLegalGuardian

        val resultFlow = useCase.invoke()

        assert(hasLegalGuardian == resultFlow.value)

        coVerifyOrder {
            mockDestinationScopedComponentManager.getEntryPoint(EidApplicationRepositoryEntryPoint::class.java, any())
            mockEidApplicationProcessEntryPoint.eidApplicationProcessRepository()
            mockEidApplicationProcessRepository.hasLegalGuardian
        }
    }
}
