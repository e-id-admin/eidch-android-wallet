package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.KeyPairError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.GetKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.VALID_KEY_PAIR
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation.CreateVcSdJwtVerifiablePresentationImpl.Companion.HASH_ALGORITHM
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance.safeJson
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.openid4vc.utils.Constants.ANDROID_KEY_STORE
import ch.admin.foitt.openid4vc.utils.createDigest
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.nimbusds.jwt.SignedJWT
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.NoSuchAlgorithmException
import java.time.Instant

class CreateVcSdJwtVerifiablePresentationImplTest {
    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockGetKeyPair: GetKeyPair

    @MockK
    private lateinit var mockCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockPresentationRequest: PresentationRequest

    private lateinit var useCase: CreateVcSdJwtVerifiablePresentationImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = CreateVcSdJwtVerifiablePresentationImpl(
            safeJson = safeJson,
            getKeyPair = mockGetKeyPair,
            defaultDispatcher = testDispatcher
        )

        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Creating VcSdJwtVerifiablePresentation without holder binding returns the presentation Jwt without key binding`() =
        runTest(testDispatcher) {
            every { mockCredential.json } returns Json.parseToJsonElement(CREDENTIAL_JSON_WITHOUT_HOLDER_BINDING)
            every { mockCredential.createVerifiableCredential(mockRequestedFields) } returns SD_JWT_WITH_DISCLOSURES
            every { mockCredential.signingKeyId } returns null

            val result = useCase(
                credential = mockCredential,
                requestedFields = mockRequestedFields,
                presentationRequest = mockPresentationRequest,
            ).assertOk()

            assertEquals(SD_JWT_WITH_DISCLOSURES, result)
        }

    @Test
    fun `Creating VcSdJwtVerifiablePresentation with holder binding returns the presentation Jwt with proof`() = runTest(testDispatcher) {
        val result = useCase(
            credential = mockCredential,
            requestedFields = mockRequestedFields,
            presentationRequest = mockPresentationRequest,
        ).assertOk()

        // The proofJwtString is not created the same every time ->
        // check that result is holderBindingSdJwtWithDisclosures plus a jwt that contains the needed values
        val resultSdJwtWithDisclosures = result.substringBeforeLast("~") + "~"
        assertEquals(HOLDER_BINDING_SD_JWT_WITH_DISCLOSURES, resultSdJwtWithDisclosures)

        val resultProofJwt = result.substringAfterLast("~")
        val jwt = SignedJWT.parse(resultProofJwt)
        val jwtHeader = jwt.header
        assertEquals(SIGNING_ALGORITHM.stdName, jwtHeader.algorithm.name)
        assertEquals(CreateVcSdJwtVerifiablePresentationImpl.HEADER_TYPE, jwtHeader.type.type)
        val jwtBody = jwt.jwtClaimsSet
        assertEquals(BASE64_URL_ENCODED_HASH, jwtBody.claims[CreateVcSdJwtVerifiablePresentationImpl.CLAIM_KEY_SD_HASH])
        assertEquals(RESPONSE_URI, jwtBody.audience.first())
        assertEquals(NONCE, jwtBody.claims[CreateVcSdJwtVerifiablePresentationImpl.CLAIM_KEY_NONCE])
        assertEquals(ISSUED_AT * 1000, jwtBody.issueTime.time)
    }

    @Test
    fun `Creating VcSdJwtVerifiablePresentation maps errors from getting verifiable credential`() = runTest(testDispatcher) {
        val exception = Exception()
        every { mockCredential.createVerifiableCredential(mockRequestedFields) } throws exception

        useCase(
            credential = mockCredential,
            requestedFields = mockRequestedFields,
            presentationRequest = mockPresentationRequest,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Creating VcSdJwtVerifiablePresentation maps errors from hashing`() = runTest(testDispatcher) {
        val exception = NoSuchAlgorithmException()
        every { any<String>().createDigest(HASH_ALGORITHM) } throws exception

        useCase(
            credential = mockCredential,
            requestedFields = mockRequestedFields,
            presentationRequest = mockPresentationRequest,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Creating VcSdJwtVerifiablePresentation maps errors from getting key pair`() = runTest(testDispatcher) {
        val exception = Exception()
        coEvery { mockGetKeyPair(any(), any()) } returns Err(KeyPairError.Unexpected(exception))

        useCase(
            credential = mockCredential,
            requestedFields = mockRequestedFields,
            presentationRequest = mockPresentationRequest,
        ).assertErrorType(PresentationRequestError.Unexpected::class)
    }

    private fun success() {
        every { mockCredential.signingKeyId } returns SIGNING_KEY_ID
        every { mockCredential.signingAlgorithm } returns SIGNING_ALGORITHM
        every { mockCredential.json } returns Json.parseToJsonElement(CREDENTIAL_JSON_WITH_HOLDER_BINDING)
        every { mockCredential.createVerifiableCredential(mockRequestedFields) } returns HOLDER_BINDING_SD_JWT_WITH_DISCLOSURES
        every { mockPresentationRequest.responseUri } returns RESPONSE_URI

        mockkStatic(String::createDigest)
        every { any<String>().createDigest(HASH_ALGORITHM) } returns BASE64_URL_ENCODED_HASH

        mockkStatic(Instant::class)
        every { Instant.now().epochSecond } returns ISSUED_AT

        coEvery { mockGetKeyPair(SIGNING_KEY_ID, ANDROID_KEY_STORE) } returns Ok(VALID_KEY_PAIR.keyPair)

        every { mockPresentationRequest.nonce } returns NONCE
    }

    private companion object {
        const val ISSUED_AT = 1L
        const val SIGNING_KEY_ID = "signingKeyId"
        val SIGNING_ALGORITHM = SigningAlgorithm.ES256
        val mockRequestedFields = mockk<List<String>>()
        const val NONCE = "nonce"
        const val RESPONSE_URI = "responseUri"

        const val CREDENTIAL_JSON_WITH_HOLDER_BINDING = """{"cnf":{"kty":"EC","crv":"P-256","x":"x","y":"y"}}"""
        const val CREDENTIAL_JSON_WITHOUT_HOLDER_BINDING = """{"key": "value"}"""

        const val CREDENTIAL_PAYLOAD_WITHOUT_HOLDER_BINDING =
            "ewogICJ0eXAiOiJ2YytzZC1qd3QiLAogICJhbGciOiJFUzI1NiIKfQ.ewogICAgICAgICAgIl9zZCI6IFsKICAgICAgICAgICAgIi1VMzJ6SEtkUTZYWTJ1TUNLNF9nOEJEZjJMSUs1VnZFYjVtR0RhZkFhRFUiLAogICAgICAgICAgICAiNXl4eWZRVHhiYlpxQlZSZ3BSNlZQdHd2Vl8tRU5mb2hEU3FfV25TMWxJbyIKICAgICAgICAgIF0sCiAgICAgICAgICAibmJmIjogMTcyMjQ5OTIwMCwKICAgICAgICAgICJfc2RfYWxnIjogInNoYS0yNTYiLAogICAgICAgICAgImV4cCI6IDE3NjcxNjgwMDAsCiAgICAgICAgICAiaWF0IjogMTcyOTI1ODQyMAogICAgICAgIH0.ZXdvZ0lDSjBlWEFpT2lKMll5dHpaQzFxZDNRaUxBb2dJQ0poYkdjaU9pSkZVekkxTmlJS2ZRLi5vcEc2TjJ6eFdRYzdwQjZKbjYwNU96bC16N0Y5VFhGeE1MUVRWOWdwUnplcnBvekNGZm1SazctaFZNbjFtUEo2aDhTbElwUlNTMGE1UXNYTElRdGU2Zw"
        const val CREDENTIAL_PAYLOAD_WITH_HOLDER_BINDING =
            "ewogICJ0eXAiOiJ2YytzZC1qd3QiLAogICJhbGciOiJFUzI1NiIKfQ.ewogICJfc2QiOiBbCiAgICAiLVUzMnpIS2RRNlhZMnVNQ0s0X2c4QkRmMkxJSzVWdkViNW1HRGFmQWFEVSIsCiAgICAiNXl4eWZRVHhiYlpxQlZSZ3BSNlZQdHd2Vl8tRU5mb2hEU3FfV25TMWxJbyIKICBdLAogICJuYmYiOiAxNzIyNDk5MjAwLAogICJfc2RfYWxnIjogInNoYS0yNTYiLAogICJleHAiOiAxNzY3MTY4MDAwLAogICJpYXQiOiAxNzI5MjU4NDIwLAogICJjbmYiOiB7CiAgICAia3R5IjogIkVDIiwKICAgICJjcnYiOiAiUC0yNTYiLAogICAgIngiOiAiLWdMMHd1dlZfOTFCQ0RfdzZra2ZjSXNyaTFtaEdBa2UwcjdNRkZ5SVM1ayIsCiAgICAieSI6ICJOcmIzMDBJV1NJOWFsX2Z2VWtQWUNqU2otUEFxMFc4UGd5TTkzMWFBeWpBIgogIH0KfQ.ZXdvZ0lDSjBlWEFpT2lKMll5dHpaQzFxZDNRaUxBb2dJQ0poYkdjaU9pSkZVekkxTmlJS2ZRLi5nVVBNTTJZTHpSSE5NU191OTFraW1qTTJuMVhMVFVueGNtdW5tVFQzRl9kT2JGWVN2WnV3YVlzLUNEWUJ5UDdZT1ktZUVBNmZwZTdpWEdMVThTeVhkdw"

        const val DISCLOSURE1 = "-U32zHKdQ6XY2uMCK4_g8BDf2LIK5VvEb5mGDafAaDU"
        const val DISCLOSURE2 = "5yxyfQTxbbZqBVRgpR6VPtwvV_-ENfohDSq_WnS1lIo"

        const val SD_JWT_WITH_DISCLOSURES = "$CREDENTIAL_PAYLOAD_WITHOUT_HOLDER_BINDING~$DISCLOSURE1~$DISCLOSURE2~"
        const val HOLDER_BINDING_SD_JWT_WITH_DISCLOSURES = "$CREDENTIAL_PAYLOAD_WITH_HOLDER_BINDING~$DISCLOSURE1~$DISCLOSURE2~"

        const val BASE64_URL_ENCODED_HASH = "base64UrlEncodedHash"
    }
}
