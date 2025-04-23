package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInformation
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchIssuerCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndSaveCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.CREDENTIAL_ISSUER
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.credentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.multipleConfigCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.multipleIdentifiersCredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.noConfigCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.noIdentifierCredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.noMatchingIdentifierCredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.oneConfigCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.oneIdentifierCredentialOffer
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchOcaBundleByFormat
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError as OpenIdCredentialOfferError

class FetchAndSaveCredentialImplTest {

    @MockK
    private lateinit var mockFetchIssuerCredentialInformation: FetchIssuerCredentialInformation

    @MockK
    private lateinit var mockFetchCredentialByConfig: FetchCredentialByConfig

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockFetchOcaBundleByFormat: FetchOcaBundleByFormat

    @MockK
    private lateinit var mockSaveCredential: SaveCredential

    private lateinit var useCase: FetchAndSaveCredential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchAndSaveCredentialImpl(
            fetchIssuerCredentialInformation = mockFetchIssuerCredentialInformation,
            fetchCredentialByConfig = mockFetchCredentialByConfig,
            environmentSetupRepository = mockEnvironmentSetupRepository,
            fetchOcaBundleByFormat = mockFetchOcaBundleByFormat,
            saveCredential = mockSaveCredential,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching credential for offer with one identifier and one matching config returns a valid id`() = runTest {
        setupDefaultMocks(
            credentialOffer = oneIdentifierCredentialOffer,
            credentialInformation = oneConfigCredentialInformation,
        )

        val result = useCase(oneIdentifierCredentialOffer)

        val credentialId = result.assertOk()
        assertEquals(CREDENTIAL_ID, credentialId)
    }

    @Test
    fun `Fetching credential for offer with multiple identifiers and multiple matching configs returns a valid id for first identifier`() = runTest {
        setupDefaultMocks(
            credentialOffer = multipleIdentifiersCredentialOffer,
            credentialInformation = multipleConfigCredentialInformation,
        )

        val result = useCase(multipleIdentifiersCredentialOffer)

        val credentialId = result.assertOk()
        assertEquals(CREDENTIAL_ID, credentialId)
    }

    @Test
    fun `Fetching credential for offer with multiple identifiers and one matching config returns a valid id`() = runTest {
        setupDefaultMocks(
            credentialOffer = multipleIdentifiersCredentialOffer,
            credentialInformation = oneConfigCredentialInformation,
        )

        val result = useCase(multipleIdentifiersCredentialOffer)

        val credentialId = result.assertOk()
        assertEquals(CREDENTIAL_ID, credentialId)
    }

    @Test
    fun `Fetching credential for offer with one identifier and multiple matching configs returns a valid id`() = runTest {
        setupDefaultMocks(
            credentialOffer = oneIdentifierCredentialOffer,
            credentialInformation = multipleConfigCredentialInformation,
        )

        val result = useCase(oneIdentifierCredentialOffer)

        val credentialId = result.assertOk()
        assertEquals(CREDENTIAL_ID, credentialId)
    }

    @Test
    fun `Fetching credential for offer with no matching identifier returns an error`() = runTest {
        setupDefaultMocks(
            credentialOffer = noMatchingIdentifierCredentialOffer,
            credentialInformation = multipleConfigCredentialInformation,
        )

        val result = useCase(noMatchingIdentifierCredentialOffer)

        result.assertErrorType(CredentialError.UnsupportedCredentialIdentifier::class)
    }

    @Test
    fun `Fetching credential for offer with no identifier returns an error`() = runTest {
        setupDefaultMocks(
            credentialOffer = noIdentifierCredentialOffer,
            credentialInformation = multipleConfigCredentialInformation,
        )

        val result = useCase(noIdentifierCredentialOffer)

        result.assertErrorType(CredentialError.UnsupportedCredentialIdentifier::class)
    }

    @Test
    fun `Fetching credential for information with no config returns an error`() = runTest {
        setupDefaultMocks(
            credentialOffer = multipleIdentifiersCredentialOffer,
            credentialInformation = noConfigCredentialInformation,
        )

        val result = useCase(multipleIdentifiersCredentialOffer)

        result.assertErrorType(CredentialError.UnsupportedCredentialIdentifier::class)
    }

    @Test
    fun `Fetching credential maps errors from fetching issuer credential information`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockFetchIssuerCredentialInformation(any(), any()) } returns
            Err(OpenIdCredentialOfferError.Unexpected(exception))

        val result = useCase(oneIdentifierCredentialOffer)

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetching credential maps errors from fetching credential by config`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockFetchCredentialByConfig(any(), any())
        } returns Err(OpenIdCredentialOfferError.Unexpected(exception))

        val result = useCase(oneIdentifierCredentialOffer)

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetching credential for prod builds does not fetch oca`() = runTest {
        coEvery { mockEnvironmentSetupRepository.fetchOca } returns false

        useCase(oneIdentifierCredentialOffer).assertOk()

        coVerify(exactly = 0) {
            mockFetchOcaBundleByFormat(any())
        }
    }

    @Test
    fun `Fetching credential maps errors from fetching oca`() = runTest {
        coEvery { mockFetchOcaBundleByFormat(any()) } returns Err(OcaError.InvalidOca)

        useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.InvalidCredentialOffer::class)
    }

    @Test
    fun `Fetching credential maps errors from saving credential`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockSaveCredential(any(), any(), any())
        } returns Err(CredentialError.Unexpected(exception))

        val result = useCase(oneIdentifierCredentialOffer)

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    private fun setupDefaultMocks(
        credentialOffer: CredentialOffer = oneIdentifierCredentialOffer,
        credentialInformation: IssuerCredentialInformation = oneConfigCredentialInformation,
    ) {
        coEvery { mockFetchIssuerCredentialInformation(CREDENTIAL_ISSUER, true) } returns
            Ok(credentialInformation)

        val vcSdJwtCredential = mockk<VcSdJwtCredential>()
        coEvery {
            mockFetchCredentialByConfig(
                credentialConfig = credentialConfig,
                credentialOffer = credentialOffer,
            )
        } returns Ok(vcSdJwtCredential)
        coEvery {
            mockFetchCredentialByConfig(
                credentialConfig = credentialConfig,
                credentialOffer = credentialOffer,
            )
        } returns Ok(vcSdJwtCredential)

        coEvery { mockEnvironmentSetupRepository.fetchOca } returns true

        coEvery { mockFetchOcaBundleByFormat(any()) } returns Ok(ocaResponse)

        coEvery {
            mockSaveCredential(
                issuerInfo = credentialInformation,
                anyCredential = vcSdJwtCredential,
                credentialConfiguration = credentialConfig
            )
        } returns Ok(CREDENTIAL_ID)
    }

    private companion object {
        const val CREDENTIAL_ID = 111L
        val ocaResponse = """{"oca": "displayData"}""".trimIndent()
    }
}
