package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.assertOkNullable
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAnyCredentialImplTest {

    @MockK
    private lateinit var mockCredentialRepository: CredentialRepo

    private lateinit var useCase: GetAnyCredentialImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetAnyCredentialImpl(mockCredentialRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting any credential matching vc+sd_jwt credential returns it`() = runTest {
        val mockCredential = Credential(
            id = CREDENTIAL_ID,
            keyBindingIdentifier = KEY_BINDING_ID,
            payload = PAYLOAD,
            format = CredentialFormat.VC_SD_JWT,
            keyBindingAlgorithm = KEY_BINDING_ALGORITHM.stdName,
            issuer = "issuer"
        )
        coEvery { mockCredentialRepository.getById(CREDENTIAL_ID) } returns Ok(mockCredential)

        val credential = useCase(CREDENTIAL_ID).assertOk()!!

        assertEquals(CREDENTIAL_ID, credential.id)
        assertEquals(KEY_BINDING_ID, credential.keyBindingIdentifier)
        assertEquals(PAYLOAD, credential.payload)
        assertEquals(KEY_BINDING_ALGORITHM, credential.keyBindingAlgorithm)
        assertTrue(credential is VcSdJwtCredential)
    }

    @Test
    fun `Getting any credential with none available returns null`() = runTest {
        coEvery { mockCredentialRepository.getById(any()) } returns Ok(null)

        val result = useCase(CREDENTIAL_ID).assertOkNullable()

        assertNull(result)
    }

    @Test
    fun `Getting any credential with other format returns an error`() = runTest {
        val mockCredential = Credential(
            id = CREDENTIAL_ID,
            keyBindingIdentifier = KEY_BINDING_ID,
            payload = PAYLOAD,
            format = CredentialFormat.UNKNOWN,
            keyBindingAlgorithm = KEY_BINDING_ALGORITHM.stdName,
            issuer = "issuer"
        )
        coEvery { mockCredentialRepository.getById(CREDENTIAL_ID) } returns Ok(mockCredential)

        val result = useCase(CREDENTIAL_ID)

        result.assertErrorType(CredentialError.Unexpected::class)
    }

    @Test
    fun `Getting any credential with unknown signing algorithm returns an error`() = runTest {
        val mockCredential = Credential(
            id = CREDENTIAL_ID,
            keyBindingIdentifier = KEY_BINDING_ID,
            payload = PAYLOAD,
            format = CredentialFormat.UNKNOWN,
            keyBindingAlgorithm = "other",
            issuer = "issuer"
        )
        coEvery { mockCredentialRepository.getById(CREDENTIAL_ID) } returns Ok(mockCredential)

        val result = useCase(CREDENTIAL_ID)

        result.assertErrorType(CredentialError.Unexpected::class)
    }

    @Test
    fun `Getting any credential maps errors from credential repository`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockCredentialRepository.getById(any()) } returns Err(SsiError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ID)

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    private companion object {
        const val CREDENTIAL_ID = 1L
        const val KEY_BINDING_ID = "privateKeyIdentifier"
        const val PAYLOAD = "ewogICJ0eXAiOiJ2YytzZC1qd3QiLAogICJhbGciOiJFUzI1NiIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJpc3MiOiJkaWQ6dGR3OmlkZW50aWZpZXIiLAogICJ2Y3QiOiJ2Y3QiCn0.ZXdvZ0lDSjBlWEFpT2lKMll5dHpaQzFxZDNRaUxBb2dJQ0poYkdjaU9pSkZVekkxTmlJc0NpQWdJbXRwWkNJNkltdGxlVWxrSWdwOS4uNHNwTXBzWE1nYlNyY0lqMFdNbXJNYXdhcVRzeG9GWmItcjdwTWlubEhvZklRRUhhS2pzV1J0dENzUTkyd0tfa3RpaDQta2VCdjdVbkc2MkRPa2NDbGc"
        val KEY_BINDING_ALGORITHM = SigningAlgorithm.ES512
    }
}
