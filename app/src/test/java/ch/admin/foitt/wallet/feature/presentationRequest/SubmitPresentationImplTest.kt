package ch.admin.foitt.wallet.feature.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.usecase.SubmitAnyCredentialPresentation
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.SubmitPresentation
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation.SubmitPresentationImpl
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError as OpenIdPresentationRequestError

class SubmitPresentationImplTest {

    @MockK
    private lateinit var mockGetAnyCredential: GetAnyCredential

    @MockK
    private lateinit var mockSubmitAnyCredentialPresentation: SubmitAnyCredentialPresentation

    @MockK
    private lateinit var mockPresentationRequest: PresentationRequest

    @MockK
    private lateinit var mockAnyCredential: AnyCredential

    private lateinit var submitPresentationUseCase: SubmitPresentation

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        submitPresentationUseCase = SubmitPresentationImpl(
            getAnyCredential = mockGetAnyCredential,
            submitAnyCredentialPresentation = mockSubmitAnyCredentialPresentation,
        )

        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Submitting a presentation for a compatible credential just runs`() = runTest {
        val result = submitPresentationUseCase(
            presentationRequest = mockPresentationRequest,
            compatibleCredential = compatibleCredential,
        )

        result.assertOk()
    }

    @Test
    fun `Submitting a presentation for a non-existing compatible credential returns an error`() = runTest {
        coEvery { mockGetAnyCredential(CREDENTIAL_ID) } returns Ok(null)

        val result = submitPresentationUseCase(
            presentationRequest = mockPresentationRequest,
            compatibleCredential = compatibleCredential,
        )

        result.assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Submitting a presentation maps errors from getting any credential`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockGetAnyCredential.invoke(any()) } returns Err(CredentialError.Unexpected(exception))

        val result = submitPresentationUseCase(
            presentationRequest = mockPresentationRequest,
            compatibleCredential = compatibleCredential,
        )

        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `Submitting a presentation maps errors from submitting any credential presentation`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockSubmitAnyCredentialPresentation(
                anyCredential = any(),
                requestedFields = any(),
                presentationRequest = any()
            )
        } returns Err(OpenIdPresentationRequestError.Unexpected(exception))

        val result = submitPresentationUseCase(
            presentationRequest = mockPresentationRequest,
            compatibleCredential = compatibleCredential,
        )

        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assertEquals(exception, error.throwable)
    }

    private fun success() {
        coEvery { mockGetAnyCredential.invoke(CREDENTIAL_ID) } returns Ok(mockAnyCredential)

        coEvery {
            mockSubmitAnyCredentialPresentation(
                anyCredential = mockAnyCredential,
                requestedFields = listOf(FIELD_KEY_1, FIELD_KEY_2),
                presentationRequest = mockPresentationRequest,
            )
        } returns Ok(Unit)
    }

    private companion object {
        const val FIELD_KEY_1 = "fieldKey1"
        const val FIELD_KEY_2 = "fieldKey2"
        val FIELD_1 = PresentationRequestField(FIELD_KEY_1, "value1")
        val FIELD_2 = PresentationRequestField(FIELD_KEY_2, "value2")
        val requestedFields = listOf(FIELD_1, FIELD_2)

        const val CREDENTIAL_ID = 1L
        val compatibleCredential = CompatibleCredential(CREDENTIAL_ID, requestedFields)
    }
}
