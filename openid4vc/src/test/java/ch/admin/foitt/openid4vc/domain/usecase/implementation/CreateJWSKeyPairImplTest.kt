package ch.admin.foitt.openid4vc.domain.usecase.implementation

import android.content.Context
import android.content.pm.PackageManager
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.usecase.CreateJWSKeyPair
import ch.admin.foitt.openid4vc.util.assertErr
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateJWSKeyPairImplTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockAppContext: Context

    private lateinit var useCase: CreateJWSKeyPair

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery {
            mockAppContext.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        } returns true

        useCase = CreateJWSKeyPairImpl(
            appContext = mockAppContext,
            defaultDispatcher = testDispatcher
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `return error if using non-existing KeyStore Provider`() = runTest(testDispatcher) {
        useCase(
            signingAlgorithm = SigningAlgorithm.ES256,
            provider = "this provider does not exist"
        ).assertErr()
    }
}
