package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.usecase.GetSignedMetadataDid
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

class GetSignedMetadataDidImplTest {

    @MockK
    private lateinit var mockDidResolverHelper: DidResolverHelper

    @MockK
    private lateinit var mockJwt: Jwt

    private lateinit var useCase: GetSignedMetadataDid

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetSignedMetadataDidImpl(
            didResolverHelper = mockDidResolverHelper,
        )

        every { mockJwt.keyId } returns KEY_ID

        coEvery {
            mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID)
        } returns Ok(DID)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the signed metadata did returns the did`() = runTest {
        val result = useCase(mockJwt).assertOk()
        assertEquals(DID, result)
    }

    @Test
    fun `Getting the signed metadata did where the did is missing the keyId header returns an error`() = runTest {
        every { mockJwt.keyId } returns null
        useCase(mockJwt).assertErrorType(CredentialOfferError.Unexpected::class)
    }

    @Test
    fun `Getting the signed metadata did maps errors from the did resolver helper`() = runTest {
        val exception = IllegalStateException("did error")
        coEvery { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID) } returns Err(exception)

        useCase(mockJwt).assertErrorType(CredentialOfferError.Unexpected::class)
    }

    private companion object {
        const val KEY_ID = "keyId"
        const val DID = "did"
    }
}
