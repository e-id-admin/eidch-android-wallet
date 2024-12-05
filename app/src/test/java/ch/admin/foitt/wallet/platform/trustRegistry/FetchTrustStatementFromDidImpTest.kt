package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryErrors
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchTrustStatementFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.FetchTrustStatementFromDidImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class FetchTrustStatementFromDidImpTest {

    @MockK
    private lateinit var mockGetTrustUrlFromDid: GetTrustUrlFromDid

    @MockK
    private lateinit var mockTrustStatementRepository: TrustStatementRepository

    @MockK
    private lateinit var mockValidateTrustStatement: ValidateTrustStatement

    private lateinit var useCase: FetchTrustStatementFromDid

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = FetchTrustStatementFromDidImpl(
            getTrustUrlFromDid = mockGetTrustUrlFromDid,
            trustStatementRepository = mockTrustStatementRepository,
            validateTrustStatement = mockValidateTrustStatement,
        )
        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching a trust statement from a did returns the first statement that can be verified`() = runTest {
        val result = useCase(issuerDid).assertOk()

        assertEquals(trustStatement2, result)
    }

    @Test
    fun `Fetching a trust statement from a did returns an error if the url can not be parsed from the did`() = runTest {
        coEvery { mockGetTrustUrlFromDid(issuerDid) } returns Err(TrustRegistryErrors.Unexpected(Exception()))

        useCase(issuerDid).assertErrorType(TrustRegistryErrors.Unexpected::class)
    }

    @Test
    fun `Fetching a trust statement from a did returns an error if the statements can not be fetched`() = runTest {
        coEvery {
            mockTrustStatementRepository.fetchTrustStatements(url)
        } returns Err(TrustRegistryErrors.Unexpected(Exception()))

        useCase(issuerDid).assertErrorType(TrustRegistryErrors.Unexpected::class)
    }

    @Test
    fun `Fetching a trust statement from a did returns an error if the fetched statement list is empty`() = runTest {
        coEvery { mockTrustStatementRepository.fetchTrustStatements(url) } returns Ok(emptyList())

        useCase(issuerDid).assertErrorType(TrustRegistryErrors.Unexpected::class)
    }

    @Test
    fun `Fetching a trust statement from a did returns an error if no statement can be verified`() = runTest {
        coEvery { mockValidateTrustStatement(any()) } returns Err(TrustRegistryErrors.Unexpected(Exception()))

        useCase(issuerDid).assertErrorType(TrustRegistryErrors.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        coEvery { mockGetTrustUrlFromDid(issuerDid) } returns Ok(url)
        coEvery { mockTrustStatementRepository.fetchTrustStatements(url) } returns Ok(trustStatementRaws)
        coEvery { mockValidateTrustStatement(trustStatementRaw1) } returns Err(TrustRegistryErrors.Unexpected(Exception()))
        coEvery { mockValidateTrustStatement(trustStatementRaw2) } returns Ok(trustStatement2)
    }

    private val issuerDid = "did:tdw:identifier"
    private val url = URL("https://example.com")
    private val trustStatementRaw1 = "truststatement1"
    private val trustStatementRaw2 = "truststatement2"
    private val trustStatementRaws = listOf(trustStatementRaw1, trustStatementRaw2)
    private val trustStatement2 = TrustStatement(SdJwt(trustStatementRaw2))
}
