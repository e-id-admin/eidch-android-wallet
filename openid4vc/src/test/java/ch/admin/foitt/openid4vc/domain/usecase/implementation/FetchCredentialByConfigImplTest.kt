package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FetchCredentialByConfigImplTest {

    @MockK
    private lateinit var mockFetchVcSdJwtCredential: FetchVcSdJwtCredential

    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockVcSdJwtCredentialConfig: VcSdJwtCredentialConfiguration

    @MockK
    private lateinit var mockCredentialOffer: CredentialOffer

    private lateinit var useCase: FetchCredentialByConfigImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchCredentialByConfigImpl(
            fetchVcSdJwtCredential = mockFetchVcSdJwtCredential,
        )

        every { mockVcSdJwtCredentialConfig.format } returns CredentialFormat.VC_SD_JWT
        coEvery {
            mockFetchVcSdJwtCredential(mockVcSdJwtCredentialConfig, mockCredentialOffer)
        } returns Ok(mockVcSdJwtCredential)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching credential by config with vc+sd_jwt config returns a valid credential`() = runTest {
        val result = useCase(mockVcSdJwtCredentialConfig, mockCredentialOffer)

        val credential = result.assertOk()
        assertEquals(mockVcSdJwtCredential, credential)
    }

    @Test
    fun `Fetching vc+sd_jwt credential by config maps error from fetching jwt vc json credential`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockFetchVcSdJwtCredential(any(), any())
        } returns Err(CredentialOfferError.Unexpected(exception))

        val result = useCase(mockVcSdJwtCredentialConfig, mockCredentialOffer)

        val error = result.assertErrorType(CredentialOfferError.Unexpected::class)
        assertEquals(exception, error.cause)
    }
}
