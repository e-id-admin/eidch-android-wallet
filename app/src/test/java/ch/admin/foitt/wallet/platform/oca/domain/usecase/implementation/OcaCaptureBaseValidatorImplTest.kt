package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCaptureBaseValidator
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.elfaExample
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OcaCaptureBaseValidatorImplTest {

    private val json = SafeJsonTestInstance.safeJson

    private lateinit var ocaCaptureBaseValidator: OcaCaptureBaseValidator

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        ocaCaptureBaseValidator = OcaCaptureBaseValidatorImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Capture bases are validated successfully`(): Unit = runTest {
        val bundle = json.safeDecodeStringTo<OcaBundle>(elfaExample).value

        ocaCaptureBaseValidator(bundle.captureBases).assertOk()
    }

    @Test
    fun `0 root capture bases returns an error`(): Unit = runTest {
        ocaCaptureBaseValidator(captureBasesWithoutRootCaptureBase).assertErrorType(OcaError.InvalidRootCaptureBase::class)
    }

    @Test
    fun `Multiple root capture bases returns an error`(): Unit = runTest {
        ocaCaptureBaseValidator(captureBasesWithMultipleRootCaptureBases).assertErrorType(OcaError.InvalidRootCaptureBase::class)
    }

    @Test
    fun `Capture base containing invalid reference returns an error`(): Unit = runTest {
        ocaCaptureBaseValidator(captureBasesWithInvalidReferenceCaptureBase)
            .assertErrorType(OcaError.InvalidCaptureBaseReferenceAttribute::class)
    }

    @Test
    fun `Capture base containing invalid array reference returns an error`(): Unit = runTest {
        ocaCaptureBaseValidator(captureBasesWithInvalidArrayReferenceCaptureBase)
            .assertErrorType(OcaError.InvalidCaptureBaseReferenceAttribute::class)
    }

    @Test
    fun `Capture bases containing references cycles returns an error`(): Unit = runTest {
        ocaCaptureBaseValidator(captureBasesWithReferenceCycles1).assertErrorType(OcaError.CaptureBaseCycleError::class)
        ocaCaptureBaseValidator(captureBasesWithReferenceCycles2).assertErrorType(OcaError.CaptureBaseCycleError::class)
        ocaCaptureBaseValidator(captureBasesWithReferenceCycles3).assertErrorType(OcaError.CaptureBaseCycleError::class)
    }

    @Test
    fun `Capture bases with more complex references return a success`(): Unit = runTest {
        ocaCaptureBaseValidator(captureBasesWithComplexReferenceGraph).assertOk()
    }

    private val captureBasesWithoutRootCaptureBase = listOf(
        CaptureBase1x0(
            digest = "validDigest",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest"),
            )
        ),
    )

    private val captureBasesWithMultipleRootCaptureBases = listOf(
        CaptureBase1x0(
            digest = "validDigest",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
            )
        ),
        CaptureBase1x0(
            digest = "validDigest2",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
            )
        ),
    )

    private val captureBasesWithInvalidReferenceCaptureBase = listOf(
        CaptureBase1x0(
            digest = "validDigest",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
            )
        ),
        CaptureBase1x0(
            digest = "validDigest2",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest"),
                "attributeKey3" to AttributeType.Reference("invalidDigest"),
            )
        ),
    )

    private val captureBasesWithInvalidArrayReferenceCaptureBase = listOf(
        CaptureBase1x0(
            digest = "validDigest",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
            )
        ),
        CaptureBase1x0(
            digest = "validDigest2",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest"),
                "attributeKey3" to AttributeType.Array(AttributeType.Reference("invalidDigest")),
            )
        ),
    )

    private val captureBasesWithReferenceCycles1 = listOf(
        CaptureBase1x0(
            digest = "validDigest",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest2"),
            )
        ),
        CaptureBase1x0(
            digest = "validDigest2",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest2"),
            )
        ),
    )

    private val captureBasesWithReferenceCycles2 = listOf(
        CaptureBase1x0(
            digest = "validDigest",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest2"),
            )
        ),
        CaptureBase1x0(
            digest = "validDigest2",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest3"),
            )
        ),
        CaptureBase1x0(
            digest = "validDigest3",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest2"),
            )
        ),
    )

    private val captureBasesWithReferenceCycles3 = listOf(
        CaptureBase1x0(
            digest = "validDigest",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest2"),
                "attributeKey3" to AttributeType.Reference("validDigest3"),
            )
        ),
        CaptureBase1x0(
            digest = "validDigest2",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest4"),
            )
        ),
        CaptureBase1x0(
            digest = "validDigest3",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest4"),
            )
        ),
        CaptureBase1x0(
            digest = "validDigest4",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest2"),
            )
        ),
    )

    private val captureBasesWithComplexReferenceGraph = listOf(
        CaptureBase1x0(
            digest = "validDigest",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest2"),
                "attributeKey3" to AttributeType.Reference("validDigest3"),
            )
        ),
        CaptureBase1x0(
            digest = "validDigest2",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest4"),
            )
        ),
        CaptureBase1x0(
            digest = "validDigest3",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest4"),
            )
        ),
        CaptureBase1x0(
            digest = "validDigest4",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
            )
        ),
    )
}
