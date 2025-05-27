package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toDisplayStatus
import ch.admin.foitt.wallet.platform.credential.domain.usecase.IsBetaIssuer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MapToCredentialDisplayDataImplTest {

    @MockK
    private lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    @MockK
    private lateinit var mockIsBetaIssuer: IsBetaIssuer

    @MockK
    private lateinit var mockCredential: Credential

    private lateinit var useCase: MapToCredentialDisplayData

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = MapToCredentialDisplayDataImpl(
            mockGetLocalizedDisplay,
            mockIsBetaIssuer,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Valid input returns credential display data`() = runTest {
        val result = useCase(mockCredential, credentialDisplays)

        val displayData = result.assertOk()
        assertEquals(CREDENTIAL_ID, displayData.credentialId)
        assertEquals(CredentialStatus.VALID.toDisplayStatus(), displayData.status)
        assertEquals(NAME, displayData.title)
        assertEquals(DESCRIPTION, displayData.subtitle)
        assertEquals(LOGO_URI, displayData.logoUri)
        assertEquals(BACKGROUND_COLOR, displayData.backgroundColor)
        assertEquals(true, displayData.isCredentialFromBetaIssuer)
    }

    @Test
    fun `Credential issuer beta check is indicated in the result`() = runTest {
        coEvery { mockIsBetaIssuer(ISSUER) } returns false

        val result = useCase(mockCredential, credentialDisplays)

        val displayData = result.assertOk()
        assertFalse(displayData.isCredentialFromBetaIssuer)
    }

    @Test
    fun `Mapping the credential display data maps errors from the GetLocalizedDisplay use case`() = runTest {
        coEvery { mockGetLocalizedDisplay(listOf(credentialDisplay)) } returns null

        val result = useCase(mockCredential, credentialDisplays)
        result.assertErrorType(CredentialError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        every { mockCredential.id } returns CREDENTIAL_ID
        every { mockCredential.status } returns CredentialStatus.VALID
        every { mockCredential.validFrom } returns 0
        every { mockCredential.validUntil } returns 17768026519L
        every { mockCredential.issuer } returns ISSUER

        coEvery { mockGetLocalizedDisplay(listOf(credentialDisplay)) } returns credentialDisplay
        coEvery { mockIsBetaIssuer(ISSUER) } returns true
    }

    private companion object {
        const val CREDENTIAL_ID = 1L
        const val ISSUER = "issuer"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val LOGO_URI = "logoUri"
        const val BACKGROUND_COLOR = "backgroundColor"

        val credentialDisplay = CredentialDisplay(
            id = 1,
            credentialId = 1,
            locale = "locale",
            name = NAME,
            description = DESCRIPTION,
            logoUri = LOGO_URI,
            backgroundColor = BACKGROUND_COLOR,
        )

        val credentialDisplays = listOf(credentialDisplay)
    }
}
