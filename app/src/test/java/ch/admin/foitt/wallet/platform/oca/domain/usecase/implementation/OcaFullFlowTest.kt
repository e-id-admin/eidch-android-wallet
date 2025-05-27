package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCaptureBaseValidator
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCesrHashValidator
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaOverlayValidator
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.elfaExample
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OcaFullFlowTest {
    private lateinit var ocaCaptureBaseValidator: OcaCaptureBaseValidator
    private lateinit var ocaOverlayValidator: OcaOverlayValidator
    private lateinit var ocaCesrHashValidator: OcaCesrHashValidator

    private val testSafeJson = SafeJsonTestInstance.safeJson
    private lateinit var ocaBundler: OcaBundler

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        ocaCesrHashValidator = OcaCesrHashValidatorImpl(safeJson = testSafeJson)
        ocaCaptureBaseValidator = OcaCaptureBaseValidatorImpl()
        ocaOverlayValidator = OcaOverlayValidatorImpl()

        ocaBundler = OcaBundlerImpl(
            json = testSafeJson,
            ocaCesrHashValidator = ocaCesrHashValidator,
            ocaCaptureBaseValidator = ocaCaptureBaseValidator,
            ocaOverlayValidator = ocaOverlayValidator
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `ELFA Oca with valid structure is processed correctly`(): Unit = runTest {
        ocaBundler(jsonString = elfaExample).assertOk()
    }
}
