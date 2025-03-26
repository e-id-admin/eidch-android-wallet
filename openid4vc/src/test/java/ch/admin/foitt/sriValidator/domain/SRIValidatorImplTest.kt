package ch.admin.foitt.sriValidator.domain

import ch.admin.foitt.sriValidator.domain.implementation.SRIValidatorImpl
import ch.admin.foitt.sriValidator.domain.implementation.SRIValidatorImpl.Companion.supportedAlgorithms
import ch.admin.foitt.sriValidator.domain.model.SRIError
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
    fun `Valid input returns true`() = runTest {
        val integrity = "sha384-H8BRh8j48O9oYatfu5AZzq6A9RINhZO5H16dQZngK7T62em8MUt1FLm52t+eX6xO"

        assertTrue(sriValidator.validate(data, integrity))
    }

    @Test
    fun `Invalid data returns false`() = runTest {
        val integrity = "sha384-XXX"

        assertFalse(sriValidator.validate(data, integrity))
    }

    @Test
    fun `Supported algorithms do not throw an exception`() = runTest {
        supportedAlgorithms.forEach { algo ->
            assertFalse(sriValidator.validate(data, "$algo-XXX"))
        }
    }

    @Test
    fun `Unsupported algorithm throws exception`() = runTest {
        val integrity = "sha123-H8BRh8j48O9oYatfu5AZzq6A9RINhZO5H16dQZngK7T62em8MUt1FLm52t+eX6xO"

        assertThrows<SRIError.UnsupportedAlgorithm> {
            sriValidator.validate(data, integrity)
        }
    }

    @Test
    fun `Malformed integrity throws exception`() = runTest {
        val integrity = "malformedIntegrity"

        assertThrows<SRIError.MalformedIntegrity> {
            sriValidator.validate(data, integrity)
        }
    }

    private companion object {
        val data = "alert('Hello, world.');".encodeToByteArray()
    }
}
