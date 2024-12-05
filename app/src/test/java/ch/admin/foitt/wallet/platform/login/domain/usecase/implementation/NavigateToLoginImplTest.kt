package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.login.domain.model.CanUseBiometricsForLoginResult
import ch.admin.foitt.wallet.platform.login.domain.usecase.CanUseBiometricsForLogin
import ch.admin.foitt.wallet.platform.login.domain.usecase.NavigateToLogin
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PassphraseLoginNavArg
import ch.admin.foitt.walletcomposedestinations.destinations.BiometricLoginScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PassphraseLoginScreenDestination
import com.github.michaelbull.result.Ok
import com.ramcosta.composedestinations.spec.Direction
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream

class NavigateToLoginImplTest {

    @MockK
    private lateinit var mockCanUseBiometricsForLogin: CanUseBiometricsForLogin

    @MockK
    private lateinit var mockResetBiometrics: ResetBiometrics

    private lateinit var useCase: NavigateToLogin

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockResetBiometrics() } returns Ok(Unit)

        useCase = NavigateToLoginImpl(
            canUseBiometricsForLogin = mockCanUseBiometricsForLogin,
            resetBiometrics = mockResetBiometrics
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @TestFactory
    fun `should navigate in correct direction`(): Stream<DynamicTest> {
        data class TestExpectation(val canUseBiometrics: CanUseBiometricsForLoginResult, val direction: Direction)

        val expectations = CanUseBiometricsForLoginResult.entries.map {
            if (it.name == CanUseBiometricsForLoginResult.Usable.name) {
                TestExpectation(it, BiometricLoginScreenDestination)
            } else {
                TestExpectation(it, PassphraseLoginScreenDestination(PassphraseLoginNavArg(biometricsLocked = false)))
            }
        }
        return expectations.stream().flatMap { testExpectation ->
            Stream.of(
                DynamicTest.dynamicTest("${testExpectation.canUseBiometrics} should navigate to ${testExpectation.direction.route}") {
                    runTest {
                        coEvery { mockCanUseBiometricsForLogin() } returns testExpectation.canUseBiometrics
                        Assertions.assertEquals(testExpectation.direction, useCase())
                    }
                }
            )
        }
    }
}
