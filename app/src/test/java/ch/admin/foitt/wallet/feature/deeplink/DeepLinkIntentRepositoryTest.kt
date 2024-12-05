package ch.admin.foitt.wallet.feature.deeplink

import ch.admin.foitt.wallet.platform.deeplink.data.DeepLinkIntentRepositoryImpl
import ch.admin.foitt.wallet.platform.deeplink.domain.repository.DeepLinkIntentRepository
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeepLinkIntentRepositoryTest {

    private lateinit var testDeepLink: String

    private lateinit var intentRepository: DeepLinkIntentRepository

    @BeforeEach
    fun setUp() {
        testDeepLink = "openid-credential-offer://credential_offer=..."
        intentRepository = DeepLinkIntentRepositoryImpl()
    }

    @Test
    fun `Set, get and delete a deepLink`() = runTest {
        intentRepository.set(testDeepLink)

        val retrievedIntent = intentRepository.get()

        Assertions.assertEquals(testDeepLink, retrievedIntent)

        intentRepository.reset()

        val retrievedIntent2 = intentRepository.get()

        Assertions.assertNull(retrievedIntent2)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }
}
