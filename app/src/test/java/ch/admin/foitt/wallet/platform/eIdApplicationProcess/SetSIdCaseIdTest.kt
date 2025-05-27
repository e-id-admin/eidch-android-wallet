package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.di.EidApplicationProcessEntryPoint
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdCurrentSIdCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.SetCurrentSIdCaseIdImpl
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SetSIdCaseIdTest {
    @MockK
    private lateinit var mockDestinationScopedComponentManager: DestinationScopedComponentManager

    @MockK
    private lateinit var mockEIdCurrentSIdCaseRepository: EIdCurrentSIdCaseRepository

    @MockK
    private lateinit var mockEidApplicationEntryPoint: EidApplicationProcessEntryPoint

    private lateinit var useCase: SetCurrentSIdCaseIdImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = SetCurrentSIdCaseIdImpl(
            destinationScopedComponentManager = mockDestinationScopedComponentManager,
        )

        coEvery {
            mockDestinationScopedComponentManager.getEntryPoint(
                EidApplicationProcessEntryPoint::class.java,
                componentScope = any()
            )
        } returns mockEidApplicationEntryPoint

        coEvery {
            mockEidApplicationEntryPoint.eidCurrentCaseRepository()
        } returns mockEIdCurrentSIdCaseRepository

        coEvery {
            mockEIdCurrentSIdCaseRepository.setCaseId(caseId = any())
        } just Runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["caseId1", "caseId2", " "],
    )
    fun `SetCurrentSIdCaseId sets the proper value in the scoped repository`(caseId: String): Unit = runTest {
        useCase(caseId = caseId)

        coVerifyOrder {
            mockDestinationScopedComponentManager.getEntryPoint(
                EidApplicationProcessEntryPoint::class.java,
                any(),
            )
            mockEidApplicationEntryPoint.eidCurrentCaseRepository()
            mockEIdCurrentSIdCaseRepository.setCaseId(caseId = caseId)
        }
    }
}
