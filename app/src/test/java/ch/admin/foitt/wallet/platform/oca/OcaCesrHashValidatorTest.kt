package ch.admin.foitt.wallet.platform.oca

import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCesrHashValidator
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.OcaCesrHashValidatorImpl
import ch.admin.foitt.wallet.platform.oca.mock.OcaCaptureBaseMocks
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErr
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class OcaCesrHashValidatorTest {

    private val safeJson = SafeJsonTestInstance.safeJson

    private lateinit var validator: OcaCesrHashValidator

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        validator = OcaCesrHashValidatorImpl(safeJson)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    fun `Valid inputs returns true`(ocaObject: String): Unit = runTest {
        val result = validator(ocaObject)
        result.assertOk()
    }

    @ParameterizedTest
    @MethodSource("validInputsDifferentOrder")
    fun `Same content in different order and formatting keep the same digest`(ocaObject: String): Unit = runTest {
        val result = validator(ocaObject)
        result.assertOk()
    }

    @Test
    fun `Validation fails on unsupported algorithm`(): Unit = runTest {
        val result = validator(OcaCaptureBaseMocks.wrongAlgorithm)
        result.assertErr()
    }

    @Test
    fun `Validation fails on invalid digests`(): Unit = runTest {
        val result = validator(OcaCaptureBaseMocks.wrongDigest)
        result.assertErr()
    }

    @Test
    fun `Validation fails on missing digest`(): Unit = runTest {
        val result = validator(OcaCaptureBaseMocks.noDigest)
        result.assertErr()
    }

    @Test
    fun `Validation fails on empty digest`(): Unit = runTest {
        val result = validator(OcaCaptureBaseMocks.emptyDigest)
        result.assertErr()
    }

    @Test
    fun `Validation fails on invalid Json`(): Unit = runTest {
        val result = validator(OcaCaptureBaseMocks.wrongJson)
        result.assertErr()
    }

    private companion object {
        @JvmStatic
        fun validInputs() = listOf(
            OcaCaptureBaseMocks.validInput01,
            OcaCaptureBaseMocks.validInput02,
            OcaCaptureBaseMocks.validInput03,
            OcaCaptureBaseMocks.validInput04,
            OcaCaptureBaseMocks.validInput05,
        )

        @JvmStatic
        fun validInputsDifferentOrder() = listOf(
            OcaCaptureBaseMocks.validInput02,
            OcaCaptureBaseMocks.validInput02alt,
        )
    }
}
