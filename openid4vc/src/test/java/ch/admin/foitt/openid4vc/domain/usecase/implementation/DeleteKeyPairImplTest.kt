package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.DeleteKeyPairError
import ch.admin.foitt.openid4vc.domain.usecase.DeleteKeyPair
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkStatic
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.KeyStore

class DeleteKeyPairImplTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockKeyStore: KeyStore

    private lateinit var useCase: DeleteKeyPair

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(KeyStore::class)
        every { KeyStore.getInstance(any()) } returns mockKeyStore
        every { mockKeyStore.load(any()) } just Runs

        useCase = DeleteKeyPairImpl(
            defaultDispatcher = testDispatcher
        )
    }

    @Test
    fun `deleting existing key returns ok`() = runTest(testDispatcher) {
        val alias = "alias"
        every { mockKeyStore.deleteEntry(alias) } just Runs
        useCase(keyId = alias).assertOk()
    }

    @Test
    fun `deleting non-existing key returns DeleteKeyPairError`() = runTest(testDispatcher) {
        every { mockKeyStore.deleteEntry("key id") } just Runs
        useCase(keyId = "unknown key id").assertErrorType(DeleteKeyPairError::class)
    }
}
