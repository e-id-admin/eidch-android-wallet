package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidateRedirectUri
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ValidateRedirectUriImplTest {

    private lateinit var useCase: ValidateRedirectUri

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = ValidateRedirectUriImpl()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "swiyu",
            "swiyudeeplink",
            "swiyu-verifydeeplink",
            "swiyu-verify",
            "someScheme:swiyu:deeplink",
            "someScheme:swiyu-verify:deeplink",
            "https://example.org/path",
            "randomString",
            "",
        ]
    )
    fun `Valid string returns ok`(input: String) = runTest {
        useCase(input).assertOk()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "swiyu:",
            "swiyu:deeplink",
            "swiyu-verify:",
            "swiyu-verify:deeplink",
        ]
    )
    fun `Invalid string returns error`(input: String) = runTest {
        useCase(input).assertErrorType(CredentialPresentationError.InvalidUri::class)
    }
}
