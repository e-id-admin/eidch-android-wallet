package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtBatchConsistency
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.swiyu.shared.consistency.SdJwtCredentialConsistencyChecker
import ch.admin.foitt.swiyu.shared.consistency.SdJwtCredentialConsistencyChecker.ConsistencyResult
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VerifyVcSdJwtBatchConsistencyImplTest {

    @MockK
    private lateinit var mockConsistencyChecker: SdJwtCredentialConsistencyChecker

    private lateinit var useCase: VerifyVcSdJwtBatchConsistency

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = VerifyVcSdJwtBatchConsistencyImpl(
            consistencyChecker = mockConsistencyChecker,
        )

        every {
            mockConsistencyChecker.checkConsistency(left = any(), right = any())
        } returns ConsistencyResult.Ok
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `An empty batch is consistent and does not invoke the checker`() {
        useCase(emptyList()).assertOk()

        verify(exactly = 0) { mockConsistencyChecker.checkConsistency(any(), any()) }
    }

    @Test
    fun `A single credential batch is consistent and does not invoke the checker`() {
        useCase(listOf(credential(PAYLOAD_1))).assertOk()

        verify(exactly = 0) { mockConsistencyChecker.checkConsistency(any(), any()) }
    }

    @Test
    fun `A batch where all credentials are consistent returns Ok`() {
        val credentials = listOf(
            credential(PAYLOAD_1),
            credential(PAYLOAD_2),
            credential(PAYLOAD_3),
        )

        useCase(credentials).assertOk()

        verify { mockConsistencyChecker.checkConsistency(left = PAYLOAD_1, right = PAYLOAD_2) }
        verify { mockConsistencyChecker.checkConsistency(left = PAYLOAD_1, right = PAYLOAD_3) }
    }

    @Test
    fun `A batch where the checker reports an inconsistency returns an error`() {
        every {
            mockConsistencyChecker.checkConsistency(left = PAYLOAD_1, right = PAYLOAD_2)
        } returns ConsistencyResult.Error

        val credentials = listOf(
            credential(PAYLOAD_1),
            credential(PAYLOAD_2),
        )

        useCase(credentials).assertErrorType(VcSdJwtError.BatchConsistencyValidationFailed::class)
    }

    @Test
    fun `A batch where the checker returns a warning is treated as inconsistent`() {
        every {
            mockConsistencyChecker.checkConsistency(left = PAYLOAD_1, right = PAYLOAD_2)
        } returns ConsistencyResult.Warn

        val credentials = listOf(
            credential(PAYLOAD_1),
            credential(PAYLOAD_2),
        )

        useCase(credentials).assertErrorType(VcSdJwtError.BatchConsistencyValidationFailed::class)
    }

    private fun credential(payload: String): VcSdJwtCredential = mockk {
        every { this@mockk.payload } returns payload
    }

    private companion object {
        const val PAYLOAD_1 = "sd-jwt-1"
        const val PAYLOAD_2 = "sd-jwt-2"
        const val PAYLOAD_3 = "sd-jwt-3"
    }
}
