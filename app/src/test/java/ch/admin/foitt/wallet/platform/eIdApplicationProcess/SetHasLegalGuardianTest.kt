package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.di.EidApplicationProcessEntryPoint
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EidApplicationProcessRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.SetHasLegalGuardianImpl
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

class SetHasLegalGuardianTest {

    @MockK
    private lateinit var mockDestinationScopedComponentManager: DestinationScopedComponentManager

    @MockK
    private lateinit var mockEidApplicationProcessRepository: EidApplicationProcessRepository

    @MockK
    private lateinit var mockEidApplicationProcessEntryPoint: EidApplicationProcessEntryPoint

    private lateinit var useCase: SetHasLegalGuardianImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = SetHasLegalGuardianImpl(
            destinationScopedComponentManager = mockDestinationScopedComponentManager,
        )

        coEvery {
            mockDestinationScopedComponentManager.getEntryPoint(
                EidApplicationProcessEntryPoint::class.java,
                componentScope = any()
            )
        } returns mockEidApplicationProcessEntryPoint

        coEvery {
            mockEidApplicationProcessEntryPoint.eidApplicationProcessRepository()
        } returns mockEidApplicationProcessRepository

        coEvery {
            mockEidApplicationProcessRepository.setHasLegalGuardian(hasLegalGuardian = any())
        } just Runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @ValueSource(
        booleans = [true, false]
    )
    fun `SetHasLegalGuardian sets the proper value in the scoped repository`(hasLegalGuardian: Boolean): Unit =
        runTest {
            useCase(hasLegalGuardian = hasLegalGuardian)

            coVerifyOrder {
                mockDestinationScopedComponentManager.getEntryPoint(
                    EidApplicationProcessEntryPoint::class.java,
                    any()
                )
                mockEidApplicationProcessEntryPoint.eidApplicationProcessRepository()
                mockEidApplicationProcessRepository.setHasLegalGuardian(hasLegalGuardian = hasLegalGuardian)
            }
        }
}
