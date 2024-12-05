package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.CredentialValidity
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredential
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.util.SafeJsonTestInstance.safeJson
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateCredentialStatusImplTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockCredentialRepository: CredentialRepo

    @MockK
    private lateinit var mockGetAnyCredential: GetAnyCredential

    @MockK
    private lateinit var mockFetchCredentialStatus: FetchCredentialStatus

    @MockK
    private lateinit var mockAnyCredential: AnyCredential

    @MockK
    private lateinit var mockJsonElement: JsonElement

    private lateinit var useCase: UpdateCredentialStatus

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = UpdateCredentialStatusImpl(
            ioDispatcher = testDispatcher,
            credentialRepository = mockCredentialRepository,
            getAnyCredential = mockGetAnyCredential,
            fetchCredentialStatus = mockFetchCredentialStatus,
            safeJson = safeJson,
        )

        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Updating the status of a valid credential updates the status to the result of the status list check`() = runTest(testDispatcher) {
        val newStatus = CredentialStatus.SUSPENDED
        coEvery { mockFetchCredentialStatus(mockAnyCredential, credentialStatusProperties) } returns Ok(newStatus)

        useCase(CREDENTIAL_ID).assertOk()

        coVerify {
            mockCredentialRepository.updateStatusByCredentialId(CREDENTIAL_ID, newStatus)
        }
    }

    @Test
    fun `Updating credential status when credential is expired updates the status to expired`() =
        runTest(testDispatcher) {
            every { mockAnyCredential.validity } returns CredentialValidity.EXPIRED

            useCase(CREDENTIAL_ID).assertOk()

            coVerify {
                mockCredentialRepository.updateStatusByCredentialId(CREDENTIAL_ID, CredentialStatus.EXPIRED)
            }
        }

    @Test
    fun `Updating credential status when credential is not yet valid updates the status to unknown`() =
        runTest(testDispatcher) {
            every { mockAnyCredential.validity } returns CredentialValidity.NOT_YET_VALID

            useCase(CREDENTIAL_ID).assertOk()

            coVerify {
                mockCredentialRepository.updateStatusByCredentialId(CREDENTIAL_ID, CredentialStatus.UNKNOWN)
            }
        }

    @Test
    fun `Updating credential status for an unknown credential status does not update the status`() = runTest(testDispatcher) {
        every { mockAnyCredential.validity } returns CredentialValidity.VALID
        coEvery {
            mockFetchCredentialStatus(mockAnyCredential, credentialStatusProperties)
        } returns Ok(CredentialStatus.UNKNOWN)

        useCase(CREDENTIAL_ID).assertOk()

        coVerify(exactly = 0) {
            mockCredentialRepository.updateStatusByCredentialId(CREDENTIAL_ID, any())
        }
    }

    @Test
    fun `Updating credential status for non-existing credential does not update status`() = runTest(testDispatcher) {
        coEvery { mockGetAnyCredential(any()) } returns Ok(null)

        coVerify(exactly = 0) {
            mockCredentialRepository.updateStatusByCredentialId(any(), any())
        }
    }

    @Test
    fun `Updating credential status maps errors from getting any credential`() = runTest(testDispatcher) {
        val exception = Exception("exception")
        coEvery { mockGetAnyCredential(any()) } returns Err(CredentialError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ID)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    @Test
    fun `Updating credential status where properties parsing fails does not update the status`() = runTest(testDispatcher) {
        every { mockJsonElement.toString() } returns "invalid"

        useCase(CREDENTIAL_ID).assertOk()

        coVerify(exactly = 0) {
            mockCredentialRepository.updateStatusByCredentialId(CREDENTIAL_ID, any())
        }
    }

    @Test
    fun `Updating credential status maps errors from fetching status update`() = runTest(testDispatcher) {
        val exception = Exception("exception")
        coEvery { mockFetchCredentialStatus(any(), any()) } returns Err(CredentialStatusError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ID)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    @Test
    fun `Updating credential status maps errors from credential update`() = runTest(testDispatcher) {
        val exception = Exception("exception")
        coEvery {
            mockCredentialRepository.updateStatusByCredentialId(CREDENTIAL_ID, any())
        } returns Err(SsiError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ID)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    private fun success() {
        every { mockAnyCredential.id } returns CREDENTIAL_ID
        every { mockAnyCredential.json } returns mockJsonElement
        every { mockJsonElement.toString() } returns STATUS_PROPERTIES
        every { mockAnyCredential.validity } returns CredentialValidity.VALID

        coEvery { mockGetAnyCredential(CREDENTIAL_ID) } returns Ok(mockAnyCredential)
        coEvery { mockFetchCredentialStatus(mockAnyCredential, credentialStatusProperties) } returns Ok(CredentialStatus.VALID)
        coEvery { mockCredentialRepository.updateStatusByCredentialId(CREDENTIAL_ID, any()) } returns Ok(CREDENTIAL_ID.toInt())
    }

    private companion object {
        const val CREDENTIAL_ID = 1L

        const val STATUS_PROPERTIES = """{
   "status":{
      "status_list":{
         "idx":0,
         "uri":"uri"
      }
   }
}"""
        val credentialStatusProperties =
            TokenStatusListProperties(
                TokenStatusListProperties.Status(
                    TokenStatusListProperties.Status.StatusList(
                        index = 0,
                        uri = "uri"
                    )
                )
            )
    }
}
