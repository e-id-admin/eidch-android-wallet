package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.ParseTokenStatusList
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ParseTokenStatusListImplTest {

    @MockK
    private lateinit var mockStatusList: TokenStatusList

    private lateinit var parseTokenStatusList: ParseTokenStatusList

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        parseTokenStatusList = ParseTokenStatusListImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Parsing token status list gets the status from a bit string with 3 bytes and bits=2`() = runTest {
        mockTokenStatusList(bits = 2, bitString = BIT_STRING)

        assertEquals(1, parseTokenStatusList(statusList = mockStatusList, index = 0).assertOk())
        assertEquals(2, parseTokenStatusList(statusList = mockStatusList, index = 1).assertOk())
        assertEquals(0, parseTokenStatusList(statusList = mockStatusList, index = 6).assertOk())
        assertEquals(3, parseTokenStatusList(statusList = mockStatusList, index = 10).assertOk())
    }

    @Test
    fun `Parsing token status list gets the status from a bit string with bits=1`() = runTest {
        mockTokenStatusList(bits = 1, bitString = BIT_STRING_SHORT)

        for (index in BIT_STRING_SHORT.indices) {
            val value = parseTokenStatusList(statusList = mockStatusList, index = index).assertOk()

            val expected = BIT_STRING_SHORT.reversed()[index].digitToInt()
            assertEquals(expected, value)
        }
    }

    @Test
    fun `Parsing token status list gets the status from a bit string with bits=4`() = runTest {
        mockTokenStatusList(bits = 4, bitString = BIT_STRING_SHORT)

        assertEquals(9, parseTokenStatusList(statusList = mockStatusList, index = 0).assertOk())
        assertEquals(12, parseTokenStatusList(statusList = mockStatusList, index = 1).assertOk())
    }

    @Test
    fun `Parsing token status list gets the status from a bit string with bits=8`() = runTest {
        mockTokenStatusList(bits = 8, bitString = BIT_STRING_SHORT)

        val value = parseTokenStatusList(statusList = mockStatusList, index = 0).assertOk()

        assertEquals(201, value)
    }

    private fun mockTokenStatusList(bits: Int, bitString: String) {
        every { mockStatusList.bits } returns bits
        every { mockStatusList.decodeAndDeflate() } returns bitStringToByteArray(bitString)
    }

    private fun bitStringToByteArray(bitString: String) = bitString.chunked(8)
        .map { it.toInt(2).toByte() }
        .toByteArray()

    companion object {
        // Create a token status list like in 9.1 of https://www.ietf.org/archive/id/draft-ietf-oauth-status-list-02.html#name-further-examples
        private const val BIT_STRING = "110010010100010011111001"
        private const val BIT_STRING_SHORT = "11001001"
    }
}
