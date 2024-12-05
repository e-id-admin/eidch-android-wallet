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
            signingKeyId = "",
            signingAlgorithm = SigningAlgorithm.ES512,
            payload = "eyJhbGciOiJFUzUxMiIsInR5cCI6IkpXVCJ9.eyJfc2QiOlsiWVJMZjYwNmNsd3Q0LWhqeUd6ZTQ5eVNGaTZWQ213YjluNWh3YjRWVUpTWSIsIlFodXZJTVFkNUx5WDhnT1Izd2VWelNZMHlHWkdHSGRWWFkwRS1OaGhVZnciXSwiX3NkX2FsZyI6InNoYS0yNTYiLCJpYXQiOjE2OTc4MDY2NzF9.APiUhTXMW6pro6Y_-aLQA120nUWK9liwf7FVCsLjiW7uKYHmjCDG3V2KGEwsjyTMjXmNWEwsamw7af-DfaCzjrOyABbG7KRfhLewJOK4UeviVbM7o8a4g0OmwzbXEFXjBVC75nY067BLvid_p6FwTxDIt9acmJtE1zW6u-HuXMFiTxPE",
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
