package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.usecase.CreateDidJwk
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.VALID_KEY_PAIR
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.openid4vc.utils.retryUseCase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RetryUseCaseTest {

    @MockK
    private lateinit var createDidJwk: CreateDidJwk

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { createDidJwk(any(), any()) } returns Ok("")
    }

    @Test
    fun `retry returns success if first iteration is successful`() = runTest {
        retryUseCase {
            createDidJwk(
                algorithm = VALID_KEY_PAIR.algorithm,
                keyPair = VALID_KEY_PAIR.keyPair
            )
        }.assertOk()

        coVerify(exactly = 1) {
            createDidJwk(any(), any())
        }
    }

    @Test
    fun `retry returns success if third iteration is successful`() = runTest {
        val errorResult = Err(CredentialOfferError.Unexpected(Exception("")))
        coEvery {
            createDidJwk(any(), any())
        } returnsMany listOf(
            errorResult, errorResult, Ok("")
        )

        retryUseCase {
            createDidJwk(
                algorithm = VALID_KEY_PAIR.algorithm,
                keyPair = VALID_KEY_PAIR.keyPair
            )
        }.assertOk()

        coVerify(exactly = 3) {
            createDidJwk(any(), any())
        }
    }

    @Test
    fun `retry returns error if all iterations fail`() = runTest {
        coEvery { createDidJwk(any(), any()) } returns Err(CredentialOfferError.Unexpected(Exception("")))

        retryUseCase {
            createDidJwk(
                algorithm = VALID_KEY_PAIR.algorithm,
                keyPair = VALID_KEY_PAIR.keyPair
            )
        }.assertErrorType(CredentialOfferError.Unexpected::class)

        coVerify(exactly = 3) {
            createDidJwk(any(), any())
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }
}
