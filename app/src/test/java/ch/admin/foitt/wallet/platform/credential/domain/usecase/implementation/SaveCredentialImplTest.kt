package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInformation
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredential
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.util.SafeJsonTestInstance.safeJson
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.assertTrue
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class SaveCredentialImplTest {

    private val mockIssuer = "issuer"
    private val mockIdentifier = "identifier"
    private val mockClaims = "claims"

    private val mockIssuerInfo = mockk<IssuerCredentialInformation> {
        every { display } returns emptyList()
        every { credentialIssuer } returns mockIssuer
    }

    private val mockAnyCredential = mockk<VcSdJwtCredential>()

    private val mockVcSdJwtCredentialConfiguration = mockk<VcSdJwtCredentialConfiguration> {
        every { display } returns emptyList()
        every { identifier } returns this@SaveCredentialImplTest.mockIdentifier
        every { claims } returns mockClaims
    }

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    lateinit var saveCredentialUseCase: SaveCredential

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        saveCredentialUseCase = SaveCredentialImpl(
            mockCredentialOfferRepository,
            safeJson,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Saving credential maps json parsing errors`() = runTest {
        val result = saveCredentialUseCase(
            issuerInfo = mockIssuerInfo,
            anyCredential = mockAnyCredential,
            credentialConfiguration = mockVcSdJwtCredentialConfiguration,
        )
        result.assertErrorType(CredentialError.UnsupportedCredentialFormat::class)
        coVerify(exactly = 0) {
            mockCredentialOfferRepository.saveCredentialOffer(any())
        }
    }

    @TestFactory
    fun `should save all claims without reserved claim names`(): List<DynamicTest> {
        coEvery { mockCredentialOfferRepository.saveCredentialOffer(any()) } returns Ok(1L)

        return payloadTestData.map { testData ->
            DynamicTest.dynamicTest(" should contain exactly these claims: ${testData.expectedClaims}") {
                runTest {
                    val anyCredential = VcSdJwtCredential(
                        keyBindingIdentifier = "",
                        keyBindingAlgorithm = SigningAlgorithm.ES256,
                        payload = testData.payload
                    )

                    val vcSdJwtCredentialConfiguration = VcSdJwtCredentialConfiguration(
                        identifier = "",
                        claims = "{}",
                        credentialSigningAlgValuesSupported = emptyList(),
                        format = CredentialFormat.VC_SD_JWT,
                        proofTypesSupported = emptyMap(),
                        vct = "",
                    )

                    val result = saveCredentialUseCase(
                        issuerInfo = mockIssuerInfo,
                        anyCredential = anyCredential,
                        credentialConfiguration = vcSdJwtCredentialConfiguration
                    )

                    result.assertOk()

                    coVerify {
                        mockCredentialOfferRepository.saveCredentialOffer(
                            coWithArg { localizedCredentialOffer ->
                                assertTrue(
                                    localizedCredentialOffer.claims.keys.map { it.key }.toSet() == testData.expectedClaims
                                ) { "Only expected claims should be saved" }
                            }
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `should call CredentialOfferRepository#saveCredentialOffer exactly one time`() = runTest {
        coEvery { mockCredentialOfferRepository.saveCredentialOffer(any()) } returns Ok(1L)

        val anyCredential = VcSdJwtCredential(
            keyBindingIdentifier = "",
            keyBindingAlgorithm = SigningAlgorithm.ES512,
            payload = "ewogICJhbGciOiJFUzUxMiIsCiAgInR5cCI6IkpXVCIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJfc2QiOlsKICAgICJZUkxmNjA2Y2x3dDQtaGp5R3plNDl5U0ZpNlZDbXdiOW41aHdiNFZVSlNZIiwKICAgICJRaHV2SU1RZDVMeVg4Z09SM3dlVnpTWTB5R1pHR0hkVlhZMEUtTmhoVWZ3IgogIF0sCiAgIl9zZF9hbGciOiJzaGEtMjU2IiwKICAiaWF0IjoxNjk3ODA2NjcxLAogICJpc3MiOiJpc3N1ZXIiLAogICJ2Y3QiOiJ2Y3QiCn0.ZXdvZ0lDSmhiR2NpT2lKRlV6VXhNaUlzQ2lBZ0luUjVjQ0k2SWtwWFZDSXNDaUFnSW10cFpDSTZJbXRsZVVsa0lncDkuLkFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUdOd0wySDZua2ZjdFNPT0NSU21HY080d3NGczNVZDJWR3phYkFySnpMSGJBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFDOFY2cGlRbDc3RnYwUVlUbTU4TmxJczMwZnNRdjc4aXRFUzNCSzR6ZnZI",
        )

        val vcSdJwtCredentialConfiguration = VcSdJwtCredentialConfiguration(
            identifier = "",
            claims = "{}",
            credentialSigningAlgValuesSupported = emptyList(),
            format = CredentialFormat.VC_SD_JWT,
            proofTypesSupported = emptyMap(),
            vct = "",
        )

        val result = saveCredentialUseCase(
            issuerInfo = mockIssuerInfo,
            anyCredential = anyCredential,
            credentialConfiguration = vcSdJwtCredentialConfiguration
        )

        result.assertOk()

        coVerify(exactly = 1) {
            mockCredentialOfferRepository.saveCredentialOffer(any())
        }
    }

    private data class PayloadTestData(val expectedClaims: Set<String>, val payload: String)

    companion object {
        private val payloadTestData = listOf(
            //  The following JWT payload contains two non-reserved claims "test1" and "test2"
            PayloadTestData(
                expectedClaims = setOf("test1", "test2"),
                payload = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImtpZCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwidGVzdDEiOiJUZXN0IiwidGVzdDIiOiJUZXN0MiIsImlhdCI6MTUxNjIzOTAyMiwiaXNzIjoiaXNzdWVyIiwidmN0IjoidmN0In0.tkJ0DaGgQSfCVRG7l1c_XdGMyR7Uov-krHkNaS_c_0xfLcdhswZQeAGxeO8Hnc_umxamUfJnlZCxLqoGZay5zA",
            ),
            //  The following JWT payload contains only claims with reserved claim names
            PayloadTestData(
                expectedClaims = emptySet(),
                payload = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImtpZCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJpc3MiOiJpc3N1ZXIiLCJ2Y3QiOiJ2Y3QifQ.nLumVkn8MPalWhI2NnGHZFJgNpWrgWsVRx4jZ31vWuMpnxh9YE5qRJ0Dxy27rUHUAF-ibJzmyr5JSBo2zdR1SA",
            ),
        )
    }
}
