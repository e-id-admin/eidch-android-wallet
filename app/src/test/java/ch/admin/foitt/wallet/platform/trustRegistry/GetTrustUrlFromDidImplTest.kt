package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryErrors
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.GetTrustUrlFromDidImpl
import ch.admin.foitt.wallet.platform.utils.BuildConfigProvider
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetTrustUrlFromDidImplTest {

    @MockK
    private lateinit var mockBuildConfigProvider: BuildConfigProvider

    private lateinit var useCase: GetTrustUrlFromDidImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetTrustUrlFromDidImpl(mockBuildConfigProvider)
        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A Did with supported base domain returns a valid Url`() {
        val result = useCase(inputWithDomain01)
        result.assertOk()
    }

    @Test
    fun `A Did with supported base domain returns the right mapping`() {
        val result = useCase(inputWithDomain03)
        val url = result.assertOk()
        assert(url.host == trustRegistryMappings.get(domain03))
    }

    @Test
    fun `A call uses the provided trust registry mapping`() {
        useCase(inputWithDomain03)

        coVerify(exactly = 1) {
            mockBuildConfigProvider.trustRegistryMapping
        }
    }

    @Test
    fun `A Did with unsupported base domain returns an error`() {
        val result = useCase(inputDidWithUnsupportedDomain)
        result.assertErrorType(TrustRegistryErrors.Unexpected::class)
    }

    @Test
    fun `An random string input returns an error`() {
        val result = useCase(inputRandom)
        result.assertErrorType(TrustRegistryErrors.Unexpected::class)
    }

    @Test
    fun `A non-did twd input returns an error`() {
        val result = useCase(inputNonDid)
        result.assertErrorType(TrustRegistryErrors.Unexpected::class)
    }

    @Test
    fun `A did input with unsupported method returns an error`() {
        val result = useCase(inputDidWithUnsupportedMethod)
        result.assertErrorType(TrustRegistryErrors.Unexpected::class)
    }

    @Test
    fun `An empty input returns an error`() {
        val result = useCase(" ")
        result.assertErrorType(TrustRegistryErrors.Unexpected::class)
    }

    private fun success() {
        coEvery { mockBuildConfigProvider.trustRegistryMapping } returns trustRegistryMappings
    }

    private val domain01 = "some.domain.bit.admin.ch"
    private val domain02 = "some.other.domain.bit.admin.ch"
    private val domain03 = "dev.other.domain.bit.admin.ch"

    private val inputWithDomain01 = "did:tdw:randomid=:$domain01:api:v1:did:randomuuid"
    private val inputWithDomain03 = "did:tdw:randomid=:$domain03:api:v1:did:randomuuid2"
    private val inputDidWithUnsupportedDomain = "did:tdw:randomid=:wrong.domain.admin.ch:api:v1:did:randomuuid"
    private val inputDidWithUnsupportedMethod = "did:web:randomid=:some.domain.bit.admin.ch:api:v1:did:randomuuid"
    private val inputNonDid = "https//twd:randomid=:some.domain.bit.admin.ch:api:v1:did:randomuuid"
    private val inputRandom = "1e4ddcb5e7670:9e8ae97e6e8/fd7df"

    private val trustRegistryMappings = mapOf<String, String>(
        domain01 to "example.org",
        domain02 to "an.example.ch",
        domain03 to "dev.org",
    )
}
