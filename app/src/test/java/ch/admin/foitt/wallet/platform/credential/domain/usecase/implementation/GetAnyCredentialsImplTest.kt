package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAnyCredentialsImplTest {

    @MockK
    private lateinit var mockCredentialRepository: CredentialRepo

    private lateinit var useCase: GetAnyCredentialsImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetAnyCredentialsImpl(mockCredentialRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting any credentials with none available returns empty list`() = runTest {
        coEvery { mockCredentialRepository.getAll() } returns Ok(emptyList())

        val result = useCase().assertOk()

        assertEquals(0, result.size)
    }

    @Test
    fun `Getting any credentials with one vc+sd_jwt available returns it`() = runTest {
        val mockCredential = Credential(
            id = CREDENTIAL_ID,
            privateKeyIdentifier = PRIVATE_KEY_ID,
            payload = PAYLOAD,
            format = CredentialFormat.VC_SD_JWT,
            signingAlgorithm = SIGNING_ALGORITHM.stdName,
        )
        coEvery { mockCredentialRepository.getAll() } returns Ok(listOf(mockCredential))

        val result = useCase().assertOk()

        assertEquals(1, result.size)
        val credential = result.first()
        assertEquals(CREDENTIAL_ID, credential.id)
        assertEquals(PRIVATE_KEY_ID, credential.signingKeyId)
        assertEquals(PAYLOAD, credential.payload)
        assertEquals(SIGNING_ALGORITHM, credential.signingAlgorithm)
        assertTrue(credential is VcSdJwtCredential)
    }

    @Test
    fun `Getting any credentials with two vc+sd_jwt available returns both`() = runTest {
        val mockCredential = Credential(
            id = CREDENTIAL_ID,
            privateKeyIdentifier = PRIVATE_KEY_ID,
            payload = PAYLOAD,
            format = CredentialFormat.VC_SD_JWT,
            signingAlgorithm = SIGNING_ALGORITHM.stdName,
        )
        val mockCredential2 = Credential(
            id = CREDENTIAL_ID_2,
            privateKeyIdentifier = PRIVATE_KEY_ID_2,
            payload = PAYLOAD_2,
            format = CredentialFormat.VC_SD_JWT,
            signingAlgorithm = SIGNING_ALGORITHM_2.stdName,
        )
        coEvery { mockCredentialRepository.getAll() } returns Ok(listOf(mockCredential, mockCredential2))

        val result = useCase().assertOk()

        assertEquals(2, result.size)

        assertEquals(CREDENTIAL_ID, result[0].id)
        assertEquals(PRIVATE_KEY_ID, result[0].signingKeyId)
        assertEquals(PAYLOAD, result[0].payload)
        assertEquals(SIGNING_ALGORITHM, result[0].signingAlgorithm)
        assertTrue(result[0] is VcSdJwtCredential)

        assertEquals(CREDENTIAL_ID_2, result[1].id)
        assertEquals(PRIVATE_KEY_ID_2, result[1].signingKeyId)
        assertEquals(PAYLOAD_2, result[1].payload)
        assertEquals(SIGNING_ALGORITHM_2, result[0].signingAlgorithm)
        assertTrue(result[1] is VcSdJwtCredential)
    }

    @Test
    fun `Getting any credentials with other format available returns an empty list`() = runTest {
        val mockCredential = Credential(
            id = CREDENTIAL_ID,
            privateKeyIdentifier = PRIVATE_KEY_ID,
            payload = PAYLOAD,
            format = CredentialFormat.UNKNOWN,
            signingAlgorithm = SIGNING_ALGORITHM.stdName,
        )
        coEvery { mockCredentialRepository.getAll() } returns Ok(listOf(mockCredential))

        val result = useCase().assertOk()
        assertEquals(emptyList<AnyCredential>(), result)
    }

    @Test
    fun `Getting any credentials with unknown signing algorithm returns an empty list`() = runTest {
        val mockCredential = Credential(
            id = CREDENTIAL_ID,
            privateKeyIdentifier = PRIVATE_KEY_ID,
            payload = PAYLOAD,
            format = CredentialFormat.VC_SD_JWT,
            signingAlgorithm = "other",
        )
        coEvery { mockCredentialRepository.getAll() } returns Ok(listOf(mockCredential))

        val result = useCase().assertOk()
        assertEquals(emptyList<AnyCredential>(), result)
    }

    @Test
    fun `Getting any credentials maps errors from credential repository`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockCredentialRepository.getAll() } returns Err(SsiError.Unexpected(exception))

        val result = useCase()

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    private companion object {
        const val CREDENTIAL_ID = 1L
        const val CREDENTIAL_ID_2 = 2L
        const val PRIVATE_KEY_ID = "privateKeyIdentifier"
        const val PRIVATE_KEY_ID_2 = "privateKeyIdentifier2"
        const val PAYLOAD = "payload"
        const val PAYLOAD_2 = "payload2"
        val SIGNING_ALGORITHM = SigningAlgorithm.ES512
        val SIGNING_ALGORITHM_2 = SigningAlgorithm.ES512
    }
}
