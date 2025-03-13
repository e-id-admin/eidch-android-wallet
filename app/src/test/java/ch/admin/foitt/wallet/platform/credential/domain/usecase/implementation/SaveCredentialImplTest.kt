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
import org.junit.jupiter.api.Test

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
}
