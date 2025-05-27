package ch.admin.foitt.wallet.platform.credential

import ch.admin.foitt.wallet.platform.credential.domain.model.getDisplayStatus
import ch.admin.foitt.wallet.platform.credential.domain.model.toDisplayStatus
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant

class GetDisplayStatusTest {

    @MockK
    private lateinit var mockCredential: Credential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { mockCredential.status } returns CredentialStatus.VALID
        every { mockCredential.validFrom } returns 0
        every { mockCredential.validUntil } returns 17768026519L
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid credential returns a valid result`() {
        val status = mockCredential.getDisplayStatus(mockCredential.status)
        assertEquals(CredentialDisplayStatus.Valid, status)
    }

    @Test
    fun `An expired validity returns an expired result`() {
        val validUntil = 1516183639L
        every { mockCredential.validUntil } returns validUntil
        val expectedInstant = Instant.ofEpochSecond(validUntil)

        val status = mockCredential.getDisplayStatus(mockCredential.status)
        assertTrue(status is CredentialDisplayStatus.Expired)
        val expectedStatus = status as CredentialDisplayStatus.Expired
        assertEquals(expectedInstant, expectedStatus.expiredAt)
    }

    @Test
    fun `A not-yet-valid validity returns a not-yet-valid result`() {
        // Sat Jan 17 2533 11:55:19 -> test will fail in ~500 years
        val validFrom = 17768026519L
        every { mockCredential.validFrom } returns validFrom
        val expectedInstant = Instant.ofEpochSecond(validFrom)

        val status = mockCredential.getDisplayStatus(mockCredential.status)
        assertTrue(status is CredentialDisplayStatus.NotYetValid)
        val expectedStatus = status as CredentialDisplayStatus.NotYetValid
        assertEquals(expectedInstant, expectedStatus.validFrom)
    }

    @Test
    fun `A non-valid validity take precedence over a status`() {
        val validUntil = 1516183639L
        every { mockCredential.validUntil } returns validUntil
        val expectedInstant = Instant.ofEpochSecond(validUntil)
        coEvery { mockCredential.status } returns CredentialStatus.REVOKED

        val status = mockCredential.getDisplayStatus(mockCredential.status)
        assertTrue(status is CredentialDisplayStatus.Expired)
        val expectedStatus = status as CredentialDisplayStatus.Expired
        assertEquals(expectedInstant, expectedStatus.expiredAt)
    }

    @ParameterizedTest
    @EnumSource(
        value = CredentialStatus::class,
    )
    fun `Given a valid validity, the status is returned`(credentialStatus: CredentialStatus) {
        coEvery { mockCredential.status } returns credentialStatus
        val displayStatus = mockCredential.getDisplayStatus(mockCredential.status)
        assertEquals(credentialStatus.toDisplayStatus(), displayStatus)
    }
}
