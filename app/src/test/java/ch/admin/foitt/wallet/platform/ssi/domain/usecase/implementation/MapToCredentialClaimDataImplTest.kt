package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimImage
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialClaim.buildCredentialClaim
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialClaim.credentialClaimDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialClaim.credentialClaimDisplays
import ch.admin.foitt.wallet.platform.utils.base64NonUrlStringToByteArray
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MapToCredentialClaimDataImplTest {

    private lateinit var mapToCredentialClaimData: MapToCredentialClaimData

    @MockK
    private lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic("ch.admin.foitt.wallet.platform.utils.ByteArrayExtKt")
        coEvery { any<String>().base64NonUrlStringToByteArray() } returns byteArrayOf()

        mapToCredentialClaimData = MapToCredentialClaimDataImpl(
            getLocalizedDisplay = mockGetLocalizedDisplay
        )

        coEvery { mockGetLocalizedDisplay(displays = credentialClaimDisplays) } returns credentialClaimDisplay
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Claim with string valueType should return correct data`() = runTest {
        val claim = buildCredentialClaim("string")

        val data = mapToCredentialClaimData(claim = claim, displays = credentialClaimDisplays).assertOk()
        assertTrue(data is CredentialClaimText, "string valueType should return ${CredentialClaimText::class.simpleName}")
        assertEquals(credentialClaimDisplay.name, data.localizedKey)
        assertEquals(claim.value, (data as CredentialClaimText).value)
    }

    @Test
    fun `Claim with bool valueType should return correct data`() = runTest {
        val claim = buildCredentialClaim("bool")

        val data = mapToCredentialClaimData(claim = claim, displays = credentialClaimDisplays).assertOk()
        assertTrue(data is CredentialClaimText, "bool valueType should return ${CredentialClaimText::class.simpleName}")
        assertEquals(credentialClaimDisplay.name, data.localizedKey)
        assertEquals(claim.value, (data as CredentialClaimText).value)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "image/png",
            "image/jpeg",
            "image/jp2",
        ]
    )
    fun `Claim with supported image mime type should return correct data`(imageMimeType: String) = runTest {
        val claim = buildCredentialClaim(imageMimeType)

        val data = mapToCredentialClaimData(claim = claim, displays = credentialClaimDisplays).assertOk()
        assertTrue(data is CredentialClaimImage, "$imageMimeType mime type should return ${CredentialClaimImage::class.simpleName}")
        assertEquals(credentialClaimDisplay.name, data.localizedKey)
        assertEquals(claim.value.base64NonUrlStringToByteArray(), (data as CredentialClaimImage).imageData)
    }

    @Test
    fun `Claim with jpg valueType should return an error`() = runTest {
        mapToCredentialClaimData(claim = buildCredentialClaim("image/jpg"), displays = credentialClaimDisplays)
            .assertErrorType(MapToCredentialClaimDataError::class)
    }

    @Test
    fun `Claim with null valueType should return an error`() = runTest {
        mapToCredentialClaimData(claim = buildCredentialClaim("null"), displays = credentialClaimDisplays)
            .assertErrorType(MapToCredentialClaimDataError::class)
    }

    @Test
    fun `Claim with empty valueType should return an error`() = runTest {
        mapToCredentialClaimData(claim = buildCredentialClaim(""), displays = credentialClaimDisplays)
            .assertErrorType(MapToCredentialClaimDataError::class)
    }

    @Test
    fun `Claim with string valueType but no displays should return an error`() = runTest {
        coEvery { mockGetLocalizedDisplay(displays = any<List<CredentialClaimDisplay>>()) } returns null
        val claim = buildCredentialClaim("string")

        mapToCredentialClaimData(claim = claim, displays = emptyList())
            .assertErrorType(MapToCredentialClaimDataError::class)
    }
}
