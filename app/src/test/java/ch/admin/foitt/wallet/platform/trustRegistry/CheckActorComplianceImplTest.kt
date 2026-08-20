package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceReasonDisplay
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.CheckActorCompliance
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateNonComplianceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.CheckActorComplianceImpl
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

class CheckActorComplianceImplTest {

    @MockK
    private lateinit var mockGetTrustDomainFromDid: GetTrustDomainFromDid

    @MockK
    private lateinit var mockTrustStatementRepository: TrustStatementRepository

    @MockK
    private lateinit var mockValidateNonComplianceTrustListStatement: ValidateNonComplianceTrustListStatement

    @MockK
    private lateinit var mockTrustStatementJwt: Jwt

    private lateinit var useCase: CheckActorCompliance

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = CheckActorComplianceImpl(
            getTrustDomainFromDid = mockGetTrustDomainFromDid,
            trustStatementRepository = mockTrustStatementRepository,
            validateNonComplianceTrustListStatement = mockValidateNonComplianceTrustListStatement,
        )

        coEvery {
            mockGetTrustDomainFromDid(any())
        } returns Ok(TRUST_DOMAIN)

        coEvery {
            mockTrustStatementRepository.fetchNonComplianceTrustListStatement(TRUST_DOMAIN)
        } returns Ok(mockTrustStatementJwt)

        coEvery {
            mockValidateNonComplianceTrustListStatement(mockTrustStatementJwt, any())
        } returns Ok(nonComplianceTrustListStatementSuccess)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Checking actor compliance for reported actor returns reported with localized reason`() = runTest {
        val result = useCase(REPORTED_ACTOR_DID)

        val expected = NonComplianceData(
            state = ActorComplianceState.REPORTED,
            reasonDisplays = nonComplianceReasonDisplays,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `Checking actor compliance for non-reported actor returns not reported`() = runTest {
        val result = useCase(NON_REPORTED_ACTOR_DID)

        val expected = NonComplianceData(
            state = ActorComplianceState.NOT_REPORTED,
            reasonDisplays = null,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `Checking actor compliance where getting the trust domain fails returns unknown`() = runTest {
        coEvery {
            mockGetTrustDomainFromDid(any())
        } returns Err(TrustRegistryError.Unexpected(IllegalStateException("error when getting trust domain")))

        val result = useCase(REPORTED_ACTOR_DID)

        val expected = NonComplianceData(
            state = ActorComplianceState.UNKNOWN,
            reasonDisplays = null,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `Checking actor compliance where fetching trust statement fails returns unknown`() = runTest {
        coEvery {
            mockTrustStatementRepository.fetchNonComplianceTrustListStatement(TRUST_DOMAIN)
        } returns Err(TrustRegistryError.Unexpected(IllegalStateException("error when fetching trust statement")))

        val result = useCase(REPORTED_ACTOR_DID)

        val expected = NonComplianceData(
            state = ActorComplianceState.UNKNOWN,
            reasonDisplays = null,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `Checking actor compliance where validating trust statement fails returns unknown`() = runTest {
        coEvery {
            mockValidateNonComplianceTrustListStatement(any(), any())
        } returns Err(TrustRegistryError.Unexpected(IllegalStateException("error when validating trust statement")))

        val result = useCase(REPORTED_ACTOR_DID)

        val expected = NonComplianceData(
            state = ActorComplianceState.UNKNOWN,
            reasonDisplays = null,
        )

        assertEquals(expected, result)
    }

    private companion object {
        const val REPORTED_ACTOR_DID = "reported actor did"
        const val NON_REPORTED_ACTOR_DID = "non reported actor did"
        const val TRUST_DOMAIN = "trustDomain.example.org"

        val nonComplianceReasonDisplays = listOf(
            NonComplianceReasonDisplay(locale = "en", "reason en"),
            NonComplianceReasonDisplay(locale = "de", "reason de"),
            NonComplianceReasonDisplay(locale = "fr", "reason fr"),
        )

        val nonComplianceTrustListStatementSuccess = NonComplianceTrustListStatement(
            typ = NonComplianceTrustListStatement.TYPE,
            alg = SignatureAlgorithm.ES256,
            kid = "kid",
            profileVersion = "1.0",
            jti = "jti",
            iat = 1L,
            exp = 2L,
            status = TokenStatusListProperties(
                statusList = TokenStatusListProperties.StatusList(
                    index = 0,
                    uri = "status-list-uri",
                ),
            ),
            nonCompliantActors = listOf(
                NonComplianceTrustListStatement.NonCompliantActor(
                    actor = REPORTED_ACTOR_DID,
                    reason = mapOf(
                        "en" to "reason en",
                        "de" to "reason de",
                        "fr" to "reason fr",
                    )
                )
            )
        )
    }
}
