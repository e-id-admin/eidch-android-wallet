package ch.admin.foitt.wallet.platform.credential

import ch.admin.foitt.openid4vc.domain.model.anycredential.CredentialValidity
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.getDisplayStatus
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.toDisplayStatus
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
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

    @MockK
    private lateinit var mockAnyCredential: VcSdJwtCredential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(Credential::toAnyCredential)
        coEvery { mockCredential.toAnyCredential() } returns mockAnyCredential
        coEvery { mockAnyCredential.validity } returns CredentialValidity.Valid
        coEvery { mockCredential.status } returns CredentialStatus.VALID
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid credential returns a valid result`() {
        val status = mockCredential.getDisplayStatus()
        assertEquals(CredentialDisplayStatus.Valid, status)
    }

    @Test
    fun `An expired validity returns an expired result`() {
        val expectedInstant = Instant.ofEpochSecond(1516183639)
        coEvery { mockAnyCredential.validity } returns CredentialValidity.Expired(expectedInstant)
        val status = mockCredential.getDisplayStatus()
        assertTrue(status is CredentialDisplayStatus.Expired)
        val expectedStatus = status as CredentialDisplayStatus.Expired
        assertEquals(expectedInstant, expectedStatus.expiredAt)
    }

    @Test
    fun `A not-yet-valid validity returns a not-yet-valid result`() {
        val expectedInstant = Instant.ofEpochSecond(17768026519)
        coEvery { mockAnyCredential.validity } returns CredentialValidity.NotYetValid(expectedInstant)

        val status = mockCredential.getDisplayStatus()
        assertTrue(status is CredentialDisplayStatus.NotYetValid)
        val expectedStatus = status as CredentialDisplayStatus.NotYetValid
        assertEquals(expectedInstant, expectedStatus.validFrom)
    }

    @Test
    fun `A non-valid validity take precedence over a status`() {
        val expectedInstant = Instant.ofEpochSecond(1706865671)
        coEvery { mockAnyCredential.validity } returns CredentialValidity.Expired(expectedInstant)
        coEvery { mockCredential.status } returns CredentialStatus.REVOKED

        val status = mockCredential.getDisplayStatus()
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
        val displayStatus = mockCredential.getDisplayStatus()
        assertEquals(credentialStatus.toDisplayStatus(), displayStatus)
    }
}
