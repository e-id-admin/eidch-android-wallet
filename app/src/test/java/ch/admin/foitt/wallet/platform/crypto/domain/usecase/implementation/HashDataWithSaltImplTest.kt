package ch.admin.foitt.wallet.platform.crypto.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.crypto.domain.model.HashConfiguration
import ch.admin.foitt.wallet.util.assertErr
import com.github.michaelbull.result.getOrThrow
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class HashDataWithSaltImplTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `hashing with salt should use default hash configuration`() = runTest(testDispatcher) {
        val algo: HashConfiguration = spyk(HashConfiguration())
        val data = "data"
        val salt = "salt".toByteArray()

        val hashData = HashDataWithSaltImpl(algo, testDispatcher).invoke(data, salt).getOrThrow { it.throwable }

        verify { algo.hashAlgorithm }
        verify { algo.hashIterations }
        verify { algo.hashKeyLength }
        verify(exactly = 0) { algo.saltLength }
        assertEquals(salt.size, hashData.salt.size)
        assertEquals(algo.hashKeyLength / 8, hashData.hash.size) // divided keyLength in bits by 8 to get bytes
        // divided keyLength in bits by 8 to get bytes; hex-string uses 2 bytes per position
        assertEquals(algo.hashKeyLength / 8, hashData.hashHexString.length / 2)
    }

    @Test
    fun `hashing with salt should use given hash configuration`() = runTest(testDispatcher) {
        val algo: HashConfiguration = spyk(HashConfiguration())
        val keyLength = 128
        every { algo.hashKeyLength } returns keyLength
        val data = "data"
        val salt = "salt".toByteArray()

        val hashData = HashDataWithSaltImpl(algo, testDispatcher).invoke(data, salt).getOrThrow { it.throwable }

        verify { algo.hashAlgorithm }
        verify { algo.hashIterations }
        verify { algo.hashKeyLength }
        verify(exactly = 0) { algo.saltLength }
        assertEquals(salt.size, hashData.salt.size)
        assertEquals(keyLength / 8, hashData.hash.size) // divided keyLength in bits by 8 to get bytes
        // divided keyLength in bits by 8 to get bytes; hex-string uses 2 bytes per position
        assertEquals(keyLength / 8, hashData.hashHexString.length / 2)
    }

    @Test
    fun `hashing with salt should fail for unknown hash algorithm`() = runTest(testDispatcher) {
        val algo: HashConfiguration = spyk(HashConfiguration())
        every { algo.hashAlgorithm } returns "unknown"
        val data = "data"
        val salt = "salt".toByteArray()

        val result = HashDataWithSaltImpl(algo, testDispatcher).invoke(data, salt)

        result.assertErr()
    }

    @Test
    fun `hashing with salt should fail for zero salt size`() = runTest(testDispatcher) {
        val algo: HashConfiguration = spyk(HashConfiguration())
        val data = "data"
        val salt = "".toByteArray()

        val result = HashDataWithSaltImpl(algo, testDispatcher).invoke(data, salt)

        result.assertErr()
    }

    @Test
    fun `hashing with salt should fail for zero hash key length`() = runTest(testDispatcher) {
        val algo: HashConfiguration = spyk(HashConfiguration())
        every { algo.hashKeyLength } returns 0
        val data = "data"
        val salt = "salt".toByteArray()

        val result = HashDataWithSaltImpl(algo, testDispatcher).invoke(data, salt)

        result.assertErr()
    }

    @Test
    fun `hashing with salt two times with same data but different salt should not generate same hash`() = runTest(
        testDispatcher
    ) {
        val algo: HashConfiguration = spyk(HashConfiguration())
        val data = "data"
        val useCase = HashDataWithSaltImpl(algo, testDispatcher)

        val hashData = useCase.invoke(data, "salt".toByteArray()).getOrThrow { it.throwable }
        val hashData2 = useCase.invoke(data, "anotherSalt".toByteArray()).getOrThrow { it.throwable }

        assertNotEquals(hashData, hashData2)
        assertFalse(hashData.hash.contentEquals(hashData2.hash))
        assertNotEquals(hashData.hashHexString, hashData2.hashHexString)
        assertFalse(hashData.salt.contentEquals(hashData2.salt))
    }

    @Test
    fun `hashing with salt two times with different data but same salt should not generate same hash`() = runTest(
        testDispatcher
    ) {
        val algo: HashConfiguration = spyk(HashConfiguration())
        val salt = "salt".toByteArray()
        val useCase = HashDataWithSaltImpl(algo, testDispatcher)

        val hashData = useCase.invoke("data", salt).getOrThrow { it.throwable }
        val hashData2 = useCase.invoke("anotherData", salt).getOrThrow { it.throwable }

        assertNotEquals(hashData, hashData2)
        assertFalse(hashData.hash.contentEquals(hashData2.hash))
        assertNotEquals(hashData.hashHexString, hashData2.hashHexString)
        assertTrue(hashData.salt.contentEquals(hashData2.salt))
    }

    @Test
    fun `hashing with salt two times with same data and salt should generate same hash`() = runTest(testDispatcher) {
        val algo: HashConfiguration = spyk(HashConfiguration())
        val data = "data"
        val salt = "salt".toByteArray()
        val useCase = HashDataWithSaltImpl(algo, testDispatcher)

        val hashData = useCase.invoke(data, salt).getOrThrow { it.throwable }
        val hashData2 = useCase.invoke(data, salt).getOrThrow { it.throwable }

        assertEquals(hashData, hashData2)
        assertTrue(hashData.hash.contentEquals(hashData2.hash))
        assertEquals(hashData.hashHexString, hashData2.hashHexString)
        assertTrue(hashData.salt.contentEquals(hashData2.salt))
    }
}
