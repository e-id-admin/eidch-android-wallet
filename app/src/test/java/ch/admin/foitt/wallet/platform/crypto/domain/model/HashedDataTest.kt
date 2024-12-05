package ch.admin.foitt.wallet.platform.crypto.domain.model

import ch.admin.foitt.wallet.platform.crypto.domain.usecase.implementation.HashDataWithSaltImpl
import com.github.michaelbull.result.getOrThrow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class HashedDataTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `get hash hex-string should be the same for same hash`() {
        val salt = "salt".toByteArray()
        val hash = "hash".toByteArray()
        val hashedData = HashedData(hash, salt)
        val hashedData2 = HashedData(hash, salt)

        assertEquals(hashedData2.hashHexString, hashedData.hashHexString)
    }

    @Test
    fun `hashing with reused salt should generate the same hash`() = runTest(testDispatcher) {
        val algo = HashConfiguration()
        val hashWithSaltUseCase = HashDataWithSaltImpl(algo, testDispatcher)
        val data = "data"

        val hashData = hashWithSaltUseCase.invoke(data = data, salt = null).getOrThrow { it.throwable }
        val hashData2 = hashWithSaltUseCase.invoke(data = data, salt = hashData.salt).getOrThrow { it.throwable }
        assertEquals(hashData, hashData2)
        assertTrue(hashData.salt.contentEquals(hashData2.salt))
        assertTrue(hashData.hash.contentEquals(hashData2.hash))
        assertEquals(hashData.hashHexString, hashData2.hashHexString)
    }
}
