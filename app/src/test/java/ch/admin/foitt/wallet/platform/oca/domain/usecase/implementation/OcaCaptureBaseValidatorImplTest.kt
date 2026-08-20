package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase1x0
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCaptureBaseValidator
import ch.admin.foitt.wallet.platform.oca.mock.ocaMocks.elfaExample
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
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

    @OptIn(UnsafeResultValueAccess::class)
    @Test
    fun `Capture bases are validated successfully`(): Unit = runTest {
        val bundle = json.safeDecodeStringTo<OcaBundle>(elfaExample).value

        ocaCaptureBaseValidator(bundle.captureBases).assertOk()
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
    fun `Capture bases with more complex references return a success`(): Unit = runTest {
        ocaCaptureBaseValidator(captureBasesWithComplexReferenceGraph).assertOk()
    }

    private val defaultCaptureBase = CaptureBase1x0(
        digest = "validDigest",
        attributes = mapOf(
            "attributeKey" to AttributeType.Text,
        )
    )

    private val defaultCaptureBase3Attributes = CaptureBase1x0(
        digest = "validDigest",
        attributes = mapOf(
            "attributeKey" to AttributeType.Text,
            "attributeKey2" to AttributeType.Reference("validDigest2"),
            "attributeKey3" to AttributeType.Reference("validDigest3"),
        )
    )

    private val captureBasesWithInvalidReferenceCaptureBase = listOf(
        defaultCaptureBase,
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
        defaultCaptureBase,
        CaptureBase1x0(
            digest = "validDigest2",
            attributes = mapOf(
                "attributeKey" to AttributeType.Text,
                "attributeKey2" to AttributeType.Reference("validDigest"),
                "attributeKey3" to AttributeType.Array(AttributeType.Reference("invalidDigest")),
            )
        ),
    )

    private val captureBasesWithComplexReferenceGraph = listOf(
        defaultCaptureBase3Attributes,
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
