package ch.admin.foitt.sriValidator.domain

import ch.admin.foitt.openid4vc.util.assertErr
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.sriValidator.domain.implementation.SRIValidatorImpl
import ch.admin.foitt.sriValidator.domain.implementation.SRIValidatorImpl.Companion.supportedAlgorithms
import ch.admin.foitt.sriValidator.domain.model.SRIError
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SRIValidatorImplTest {

    private lateinit var sriValidator: SRIValidator

    @BeforeEach
    fun setup() {
        sriValidator = SRIValidatorImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Valid input returns Ok`() = runTest {
        val integrity = "sha384-H8BRh8j48O9oYatfu5AZzq6A9RINhZO5H16dQZngK7T62em8MUt1FLm52t+eX6xO"

        sriValidator(data, integrity).assertOk()
    }

    @Test
    fun `Invalid data returns an error`() = runTest {
        val integrity = "sha384-XXX"

        sriValidator(data, integrity).assertErrorType(SRIError.ValidationFailed::class)
    }

    @Test
    fun `Supported algorithms do not return an error`() = runTest {
        supportedAlgorithms.forEach { algo ->
            sriValidator(data, "$algo-XXX").assertErr()
        }
    }

    @Test
    fun `Unsupported algorithm returns an error`() = runTest {
        val integrity = "sha123-H8BRh8j48O9oYatfu5AZzq6A9RINhZO5H16dQZngK7T62em8MUt1FLm52t+eX6xO"

        sriValidator(data, integrity).assertErrorType(SRIError.UnsupportedAlgorithm::class)
    }

    @Test
    fun `Malformed integrity returns an error`() = runTest {
        val integrity = "malformedIntegrity"

        sriValidator(data, integrity).assertErrorType(SRIError.MalformedIntegrity::class)
    }

    private companion object {
        val data = "alert('Hello, world.');".encodeToByteArray()
    }
}
