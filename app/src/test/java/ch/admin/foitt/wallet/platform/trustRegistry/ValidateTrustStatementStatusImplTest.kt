package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement.Companion.CLAIM_NAME_STATUS
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateTrustStatementStatusImpl
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidateTrustStatementStatusImplTest {
    @MockK
    private lateinit var mockDidResolverHelper: DidResolverHelper

    @MockK
    private lateinit var mockFetchCredentialStatus: FetchCredentialStatus

    @MockK
    private lateinit var mockTrustStatement: Jwt

    private val safeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: ValidateTrustStatementStatus

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = ValidateTrustStatementStatusImpl(
            safeJson = safeJson,
            didResolverHelper = mockDidResolverHelper,
            fetchCredentialStatus = mockFetchCredentialStatus,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid identity trust statement passes validation`() = runTest {
        val result = useCase(mockTrustStatement).assertOk()

        assertEquals(KEY_ID, result.kid)
        assertEquals(credentialStatusProperties, result.status)
    }

    @Test
    fun `A jwt missing the status returns an error`() = runTest {
        every { mockTrustStatement.payloadJson } returns buildJsonObject {}

        useCase(
            trustStatement = mockTrustStatement,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt where the status can not be parsed returns an error`() = runTest {
        every { mockTrustStatement.payloadJson } returns buildJsonObject {
            put(CLAIM_NAME_STATUS, JsonPrimitive("invalid status json"))
        }

        useCase(
            trustStatement = mockTrustStatement,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt missing the kid returns an error`() = runTest {
        every { mockTrustStatement.keyId } returns null

        useCase(
            trustStatement = mockTrustStatement,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Validation maps errors from did resolver helper`() = runTest {
        val exception = IllegalStateException("did error")
        coEvery {
            mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID)
        } returns Err(exception)

        useCase(
            trustStatement = mockTrustStatement,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Validation maps errors from status fetching`() = runTest {
        val exception = IllegalStateException("did error")
        coEvery {
            mockFetchCredentialStatus(any(), any())
        } returns Err(CredentialStatusError.Unexpected(exception))

        useCase(
            trustStatement = mockTrustStatement,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        every { mockTrustStatement.keyId } returns KEY_ID
        every { mockTrustStatement.payloadJson } returns payload

        coEvery {
            mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID)
        } returns Ok(ISSUER_DID)

        coEvery {
            mockFetchCredentialStatus(
                credentialIssuer = ISSUER_DID,
                properties = credentialStatusProperties,
            )
        } returns Ok(CredentialStatus.VALID)
    }

    private companion object {
        const val KEY_ID = "keyId"
        const val ISSUER_DID = "issuer did"
        val credentialStatusProperties = TokenStatusListProperties(
            statusList = TokenStatusListProperties.StatusList(
                index = 0,
                uri = "https://example.com/status"
            )
        )

        val payload = buildJsonObject {
            put(CLAIM_NAME_STATUS, credentialStatusProperties.toJsonObject())
        }

        private fun TokenStatusListProperties.toJsonObject(): JsonObject = SafeJsonTestInstance.json.encodeToJsonElement(
            value = this
        ).jsonObject
    }
}
