package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.di.EidApplicationProcessEntryPoint
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdCurrentSIdCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.GetCurrentSIdCaseIdImpl
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

class GetSIdCaseIdTest {
    @MockK
    private lateinit var mockDestinationScopedComponentManager: DestinationScopedComponentManager

    @MockK
    private lateinit var mockEIdCurrentSIdCaseRepository: EIdCurrentSIdCaseRepository

    @MockK
    private lateinit var mockEidApplicationProcessEntryPoint: EidApplicationProcessEntryPoint

    private lateinit var mockFlow: MutableStateFlow<String?>

    private lateinit var useCase: GetCurrentSIdCaseIdImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockFlow = MutableStateFlow(null)

        useCase = GetCurrentSIdCaseIdImpl(
            destinationScopedComponentManager = mockDestinationScopedComponentManager,
        )

        coEvery {
            mockDestinationScopedComponentManager.getEntryPoint(
                EidApplicationProcessEntryPoint::class.java,
                componentScope = any()
            )
        } returns mockEidApplicationProcessEntryPoint

        coEvery {
            mockEidApplicationProcessEntryPoint.eidCurrentCaseRepository()
        } returns mockEIdCurrentSIdCaseRepository

        coEvery {
            mockEIdCurrentSIdCaseRepository.caseId
        } returns mockFlow
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["caseId1", "caseId2", " "]
    )
    fun `GetCurrentSIdCaseId is getting the proper value from the repository`(caseId: String?): Unit = runTest {
        mockFlow.value = caseId

        val resultFlow = useCase.invoke()

        assert(caseId == resultFlow.value)

        coVerifyOrder {
            mockDestinationScopedComponentManager.getEntryPoint(
                EidApplicationProcessEntryPoint::class.java,
                any(),
            )
            mockEidApplicationProcessEntryPoint.eidCurrentCaseRepository()
            mockEIdCurrentSIdCaseRepository.caseId
        }
    }
}
