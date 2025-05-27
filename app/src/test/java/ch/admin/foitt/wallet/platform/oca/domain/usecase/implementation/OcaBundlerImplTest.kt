package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.OverlaySpecType
import ch.admin.foitt.wallet.platform.oca.domain.model.overlays.Overlay
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCaptureBaseValidator
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCesrHashValidator
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaOverlayValidator
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.elfaCaptureBase
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.elfaExample
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OcaBundlerImplTest {

    @MockK
    private lateinit var mockOcaCesrHashValidator: OcaCesrHashValidator

    @MockK
    private lateinit var mockOcaCaptureBaseValidator: OcaCaptureBaseValidator

    @MockK
    private lateinit var mockOcaOverlayValidator: OcaOverlayValidator

    private val json = SafeJsonTestInstance.safeJson
    private lateinit var ocaBundler: OcaBundler

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        ocaBundler = OcaBundlerImpl(
            json = json,
            ocaCesrHashValidator = mockOcaCesrHashValidator,
            ocaCaptureBaseValidator = mockOcaCaptureBaseValidator,
            ocaOverlayValidator = mockOcaOverlayValidator,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Oca with valid structure is processed correctly`(): Unit = runTest {
        ocaBundler(elfaExample).assertOk()
    }

    @Test
    fun `Invalid decoding of oca string to json object returns an error`(): Unit = runTest {
        ocaBundler("not a json object").assertErrorType(OcaError.InvalidJsonObject::class)
    }

    @Test
    fun `Oca json not containing a capture_bases array returns an error`(): Unit = runTest {
        ocaBundler(ocaBundleWithoutCaptureBases).assertErrorType(OcaError.InvalidJsonObject::class)
    }

    @Test
    fun `Failing CESR validation returns an error`(): Unit = runTest {
        coEvery {
            mockOcaCesrHashValidator(json.safeDecodeStringTo<JsonObject>(elfaCaptureBase).value.toString())
        } returns Err(OcaError.InvalidCESRHash("message"))

        ocaBundler(elfaExample).assertErrorType(OcaError.InvalidCESRHash::class)
    }

    @Test
    fun `Oca that can not be decoded to json object returns an error`() = runTest {
        ocaBundler(ocaBundleNotParsable).assertErrorType(OcaError.InvalidJsonObject::class)
    }

    @Test
    fun `Oca that contains unsupported overlays returns the bundle without those`() = runTest {
        val result = ocaBundler(ocaBundleWithUnsupportedOverlay).assertOk()

        val overlays = result.overlays
        assertEquals(1, overlays.size)
        assertEquals(OverlaySpecType.LABEL_1_0, overlays[0].type)
    }

    @Test
    fun `Oca bundler maps errors from oca capture base validator`() = runTest {
        coEvery { mockOcaCaptureBaseValidator(any()) } returns Err(OcaError.InvalidRootCaptureBase)

        ocaBundler(elfaExample).assertErrorType(OcaError.InvalidCaptureBases::class)
    }

    @Test
    fun `Oca bundler maps errors from oca overlay validator`() = runTest {
        coEvery { mockOcaOverlayValidator(any()) } returns Err(OcaError.MissingMandatoryOverlay)

        ocaBundler(elfaExample).assertErrorType(OcaError.InvalidOverlays::class)
    }

    private fun setupDefaultMocks() {
        val captureBases = listOf(json.safeDecodeStringTo<CaptureBase>(standardCaptureBase).value)
        val overlays = listOf(json.safeDecodeStringTo<Overlay>(standardOverlay).value)

        coEvery { mockOcaCesrHashValidator(any()) } returns Ok(Unit)
        coEvery { mockOcaCaptureBaseValidator(any()) } returns Ok(captureBases)
        coEvery { mockOcaOverlayValidator(any()) } returns Ok(overlays)
    }

    private val standardCaptureBase = """
        {
          "type": "spec/capture_base/1.0",
          "digest": "digest",
          "attributes": {
            "attributeKey": "Text"
          }
        }
    """.trimIndent()

    private val standardOverlay = """
        {
          "capture_base": "digest",
          "type": "spec/overlays/label/1.0",
          "language": "en",
          "attribute_labels" : {
            "attributeKey" : "label"
          }
        }
    """.trimIndent()

    private val ocaBundleWithoutCaptureBases = """
        {
          "overlays": [
            $standardOverlay
          ]
        }
    """.trimIndent()

    private val ocaBundleNotParsable = """
        {
          "capture_bases": [
            $standardCaptureBase
          ]
        }
    """.trimIndent()

    private val ocaBundleWithUnsupportedOverlay = """
        {
          "capture_bases": [
            $standardCaptureBase
          ],
          "overlays": [
            {
              "capture_base": "digest",
              "type": "spec/overlays/unsupported/1.0",
              "someKey": "someValue"
            },
            $standardOverlay
          ]
        }
    """.trimIndent()
}
