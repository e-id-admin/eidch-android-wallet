package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import android.content.Context
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.toPointerString
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toDisplayStatus
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.usecase.ResolveClaimTemplate
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedAndThemedDisplay
import ch.admin.foitt.wallet.platform.theme.domain.model.Theme
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class MapToCredentialDisplayDataImplTest {
    @MockK
    private lateinit var mockAppContext: Context

    @MockK
    private lateinit var mockGetLocalizedAndThemedDisplay: GetLocalizedAndThemedDisplay

    @MockK
    private lateinit var mockResolveClaimTemplate: ResolveClaimTemplate

    @MockK
    private lateinit var mockGetActorEnvironment: GetActorEnvironment

    @MockK
    private lateinit var mockVerifiableCredential: VerifiableCredentialEntity

    private lateinit var claims: List<CredentialClaimWithDisplays>
    private lateinit var claimsWithNullValue: List<CredentialClaimWithDisplays>

    private lateinit var useCase: MapToCredentialDisplayData

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = MapToCredentialDisplayDataImpl(
            resolveClaimTemplate = mockResolveClaimTemplate,
            getActorEnvironment = mockGetActorEnvironment,
            context = mockAppContext,
            getLocalizedAndThemedDisplay = mockGetLocalizedAndThemedDisplay,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Valid input returns credential display data`() = runTest {
        val result =
            useCase(mockVerifiableCredential, credentialDisplays, claims, CredentialFormat.VC_SD_JWT, CredentialStatus.VALID)

        val displayData = result.assertOk()
        assertEquals(CREDENTIAL_ID, displayData.credentialId)
        assertEquals(CredentialStatus.VALID.toDisplayStatus(), displayData.status)
        assertEquals(NAME, displayData.title)
        assertEquals(RESOLVED_DESCRIPTION, displayData.subtitle)
        assertEquals(LOGO_URI, displayData.logoUri)
        assertEquals(BACKGROUND_COLOR, displayData.backgroundColor)
        assertEquals(ActorEnvironment.PRODUCTION, displayData.actorEnvironment)
        assertEquals(progressionState, displayData.progressionState)
    }

    @Test
    fun `With null description the template resolving is not called`() = runTest {
        val display = createCredentialDisplay()
        val displays = listOf(display)
        coEvery {
            mockGetLocalizedAndThemedDisplay(credentialDisplays = displays, preferredTheme = Theme.LIGHT)
        } returns display

        val result =
            useCase(mockVerifiableCredential, displays, claims, CredentialFormat.VC_SD_JWT, CredentialStatus.VALID)

        val displayData = result.assertOk()
        assertEquals(null, displayData.subtitle)

        coVerify(exactly = 0) { mockResolveClaimTemplate(any(), any()) }
    }

    @TestFactory
    fun `Credential issuer environment check is indicated in the result`(): List<DynamicTest> {
        val input = listOf(
            ActorEnvironment.PRODUCTION,
            ActorEnvironment.BETA,
            ActorEnvironment.EXTERNAL
        )
        return input.map { actorEnvironment ->
            DynamicTest.dynamicTest("Environment: ${actorEnvironment.name} should be correctly displayed") {
                runTest {
                    coEvery { mockGetActorEnvironment(ISSUER) } returns actorEnvironment

                    val result =
                        useCase(mockVerifiableCredential, credentialDisplays, claims, CredentialFormat.VC_SD_JWT, CredentialStatus.VALID)

                    val displayData = result.assertOk()
                    assertEquals(actorEnvironment, displayData.actorEnvironment)
                }
            }
        }
    }

    @Test
    fun `Mapping the credential display data maps errors from the GetLocalizedDisplay use case`() = runTest {
        coEvery {
            mockGetLocalizedAndThemedDisplay(listOf(element = credentialDisplay), preferredTheme = Theme.LIGHT)
        } returns null

        val result =
            useCase(mockVerifiableCredential, credentialDisplays, claims, CredentialFormat.VC_SD_JWT, CredentialStatus.VALID)
        result.assertErrorType(CredentialError.Unexpected::class)
    }

    @Test
    fun `Mapping the credential display data correctly resolves a simple template`() = runTest {
        val credentialDisplays = listOf(credentialDisplaySimpleTemplateClaimsPathPointer)
        coEvery {
            mockGetLocalizedAndThemedDisplay(credentialDisplays = credentialDisplays, preferredTheme = Theme.LIGHT)
        } returns credentialDisplaySimpleTemplateClaimsPathPointer

        val result = useCase(
            verifiableCredential = mockVerifiableCredential,
            credentialDisplays = credentialDisplays,
            claims = claims,
            credentialFormat = CredentialFormat.VC_SD_JWT,
            status = CredentialStatus.VALID,
        ).assertOk()

        assertEquals("Test: value1", result.subtitle)
    }

    @Test
    fun `Mapping the credential display data correctly resolves a multi template`() = runTest {
        val credentialDisplays = listOf(credentialDisplayMultiTemplateClaimsPathPointer)
        coEvery {
            mockGetLocalizedAndThemedDisplay(credentialDisplays = credentialDisplays, preferredTheme = Theme.LIGHT)
        } returns credentialDisplayMultiTemplateClaimsPathPointer

        val claims = listOf(
            CredentialClaimWithDisplays(
                claim = claim1,
                displays = listOf(claimDisplay)
            ),
            CredentialClaimWithDisplays(
                claim = claim2,
                displays = listOf(claimDisplay)
            )
        )

        val result = useCase(
            verifiableCredential = mockVerifiableCredential,
            credentialDisplays = credentialDisplays,
            claims = claims,
            credentialFormat = CredentialFormat.VC_SD_JWT,
            status = CredentialStatus.VALID,
        ).assertOk()

        assertEquals("Test: value1, value2", result.subtitle)
    }

    @TestFactory
    fun `Mapping the credential display where the template references an unknown key is replaced by empty string`() = runTest {
        val credentialDisplays = listOf(credentialDisplayUnknownKeyClaimsPathPointer)
        coEvery {
            mockGetLocalizedAndThemedDisplay(credentialDisplays = credentialDisplays, preferredTheme = Theme.LIGHT)
        } returns credentialDisplayUnknownKeyClaimsPathPointer

        val result = useCase(
            verifiableCredential = mockVerifiableCredential,
            credentialDisplays = credentialDisplays,
            claims = claims,
            credentialFormat = CredentialFormat.VC_SD_JWT,
            status = CredentialStatus.VALID,
        ).assertOk()

        assertEquals("Test: ", result.subtitle)
    }

    @Test
    fun `Mapping the credential display where the template references an null claim is replaced by hyphen`() = runTest {
        val credentialDisplays = listOf(credentialDisplaySimpleTemplateClaimsPathPointer)
        coEvery {
            mockGetLocalizedAndThemedDisplay(credentialDisplays = credentialDisplays, preferredTheme = Theme.LIGHT)
        } returns credentialDisplaySimpleTemplateClaimsPathPointer

        val result = useCase(
            verifiableCredential = mockVerifiableCredential,
            credentialDisplays = credentialDisplays,
            claims = claimsWithNullValue,
            credentialFormat = CredentialFormat.VC_SD_JWT,
            status = CredentialStatus.VALID,
        ).assertOk()

        assertEquals("Test: –", result.subtitle)
    }

    @TestFactory
    fun `Mapping the credential display correctly resolves nested templates`(): List<DynamicTest> {
        val input = listOf(
            "{{[\"claim2Key\"] {{[\"claim1Key\"]}}}}" to "{{[\"claim2Key\"] value1}}",
            "{{{[\"claim1Key\"]}}}" to "{value1}",
            "{{{{[\"claim1Key\"]}}}}" to "{{value1}}",
            "{{{[\"claim1Key\"]}}}}" to "{value1}}",
        )

        return input.map { (template, resolvedResult) ->
            DynamicTest.dynamicTest("template $template is resolved correctly") {
                runTest {
                    val credentialDisplay = createCredentialDisplay(template)
                    val credentialDisplays = listOf(credentialDisplay)
                    coEvery {
                        mockGetLocalizedAndThemedDisplay(credentialDisplays = credentialDisplays, preferredTheme = Theme.LIGHT)
                    } returns credentialDisplay

                    val result = useCase(
                        mockVerifiableCredential,
                        credentialDisplays,
                        claims,
                        CredentialFormat.VC_SD_JWT,
                        CredentialStatus.VALID,
                    ).assertOk()

                    assertEquals(resolvedResult, result.subtitle)
                }
            }
        }
    }

    @TestFactory
    fun `Mapping the credential display does not match and resolve invalid templates`(): List<DynamicTest> {
        val input = listOf(
            "{{}}", // empty
            "{{ }}", // empty
            "{{[\"claim1Key\"] }}", // whitespace
            "{[\"claim1Key\"]}", // single brackets
            "{[\"claim1Key\"]}}", // missing opening bracket
            "{{[\"claim1Key\"]}", // missing closing bracket
        )

        return input.map { template ->
            DynamicTest.dynamicTest("template $template is not resolved") {
                runTest {
                    val credentialDisplayInvalidTemplate = createCredentialDisplay(template)
                    val credentialDisplays = listOf(credentialDisplayInvalidTemplate)
                    coEvery {
                        mockGetLocalizedAndThemedDisplay(credentialDisplays = credentialDisplays, preferredTheme = Theme.LIGHT)
                    } returns credentialDisplayInvalidTemplate

                    val result = useCase(
                        mockVerifiableCredential,
                        credentialDisplays,
                        claims,
                        CredentialFormat.VC_SD_JWT,
                        CredentialStatus.VALID,
                    ).assertOk()

                    assertEquals(template, result.subtitle)
                }
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun setupDefaultMocks() {
        every { mockAppContext.resources.configuration.isNightModeActive } returns false
        every { mockVerifiableCredential.credentialId } returns CREDENTIAL_ID
        every { mockVerifiableCredential.validFrom } returns 0
        every { mockVerifiableCredential.validUntil } returns 17768026519L
        every { mockVerifiableCredential.issuer } returns ISSUER
        every { mockVerifiableCredential.progressionState } returns progressionState
        every { mockVerifiableCredential.createdAt } returns CREATED_AT

        claims = listOf(
            CredentialClaimWithDisplays(
                claim = claim1,
                displays = listOf(claimDisplay)
            )
        )

        claimsWithNullValue = listOf(
            CredentialClaimWithDisplays(
                claim = claimWithNullValue,
                displays = listOf(claimDisplay)
            )
        )

        every {
            mockResolveClaimTemplate(any(), any(), any())
        } answers {
            val template = firstArg<String>()
            val passedClaims = secondArg<List<CredentialClaimWithDisplays>>()
            val claim1Value = passedClaims.firstOrNull { it.claim.path == claim1PathString }?.claim?.value

            when (template) {
                DESCRIPTION -> RESOLVED_DESCRIPTION
                "Test: {{[\"claim1Key\"]}}" -> if (claim1Value == null) "Test: –" else "Test: value1"

                "Test: {{[\"claim1Key\"]}}, {{[\"claim2Key\"]}}" -> "Test: value1, value2"

                "Test: {{[\"claim3Key\"]}}" -> "Test: "

                "{{[\"claim2Key\"] {{[\"claim1Key\"]}}}}" -> "{{[\"claim2Key\"] value1}}"
                "{{{[\"claim1Key\"]}}}" -> "{value1}"
                "{{{{[\"claim1Key\"]}}}}" -> "{{value1}}"
                "{{{[\"claim1Key\"]}}}}" -> "{value1}}"
                else -> template
            }
        }

        coEvery {
            mockGetLocalizedAndThemedDisplay(
                credentialDisplays = listOf(credentialDisplay),
                preferredTheme = Theme.LIGHT
            )
        } returns credentialDisplay
        coEvery { mockGetActorEnvironment(ISSUER) } returns ActorEnvironment.PRODUCTION
    }

    private companion object {
        const val CREDENTIAL_ID = 1L
        const val ISSUER = "issuer"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val RESOLVED_DESCRIPTION = "resolved description"
        const val LOGO_URI = "logoUri"
        const val BACKGROUND_COLOR = "backgroundColor"
        const val CREATED_AT = 1L
        val progressionState = VerifiableProgressionState.ACCEPTED
        val claim1Path = listOf(ClaimsPathPointerComponent.String("claim1Key"))
        val claim1PathString = claim1Path.toPointerString()
        val claim2Path = listOf(ClaimsPathPointerComponent.String("claim2Key"))
        val claim2PathString = claim2Path.toPointerString()

        val credentialDisplay = createCredentialDisplay(DESCRIPTION)

        val credentialDisplays = listOf(credentialDisplay)

        val credentialDisplaySimpleTemplateClaimsPathPointer = createCredentialDisplay("Test: {{[\"claim1Key\"]}}")

        val credentialDisplayMultiTemplateClaimsPathPointer =
            createCredentialDisplay("Test: {{[\"claim1Key\"]}}, {{[\"claim2Key\"]}}")

        val credentialDisplayUnknownKeyClaimsPathPointer = createCredentialDisplay("Test: {{[\"claim3Key\"]}}")

        val claim1 = CredentialClaim(
            clusterId = 1,
            path = claim1PathString,
            value = "value1",
            valueType = "string"
        )

        val claim2 = CredentialClaim(
            clusterId = 1,
            path = claim2PathString,
            value = "value2",
            valueType = "string"
        )

        val claimWithNullValue = CredentialClaim(
            clusterId = 1,
            path = claim1PathString,
            value = null,
            valueType = "string"
        )

        val claimDisplay = CredentialClaimDisplay(
            claimId = 1,
            name = "name1",
            locale = "locale1",
            value = null,
        )

        fun createCredentialDisplay(template: String? = null) = CredentialDisplay(
            id = 1,
            credentialId = 1,
            locale = "locale",
            name = NAME,
            description = template,
            logoUri = LOGO_URI,
            backgroundColor = BACKGROUND_COLOR,
        )
    }
}
