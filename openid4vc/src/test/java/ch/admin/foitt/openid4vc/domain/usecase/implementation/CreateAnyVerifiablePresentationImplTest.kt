package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtVerifiablePresentation
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
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

class CreateAnyVerifiablePresentationImplTest {

    @MockK
    private lateinit var mockCreateVcSdJwtVerifiablePresentation: CreateVcSdJwtVerifiablePresentation

    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockAnyCredential: AnyCredential

    @MockK
    private lateinit var mockPresentationRequest: PresentationRequest

    private lateinit var useCase: CreateAnyVerifiablePresentationImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = CreateAnyVerifiablePresentationImpl(mockCreateVcSdJwtVerifiablePresentation)

        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Submitting presentation for vc+sd_jwt credential returns verifiable presentation`() = runTest {
        val result = useCase(
            anyCredential = mockVcSdJwtCredential,
            requestedFields = requestedFields,
            presentationRequest = mockPresentationRequest
        ).assertOk()

        assertEquals(VERIFIABLE_PRESENTATION, result)
    }

    @Test
    fun `Submitting presentation for unsupported credential format returns error`() = runTest {
        val result = useCase(
            anyCredential = mockAnyCredential,
            requestedFields = requestedFields,
            presentationRequest = mockPresentationRequest
        )

        result.assertErrorType(PresentationRequestError.Unexpected::class)
    }

    private fun success() {
        coEvery {
            mockCreateVcSdJwtVerifiablePresentation(
                credential = mockVcSdJwtCredential,
                requestedFields = requestedFields,
                presentationRequest = mockPresentationRequest,
            )
        } returns Ok(VERIFIABLE_PRESENTATION)
    }

    private companion object {
        const val FIELD = "field"
        val requestedFields = listOf(FIELD)

        const val VERIFIABLE_PRESENTATION = "verifiablePresentation"
    }
}
