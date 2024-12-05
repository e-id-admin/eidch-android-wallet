package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInformation
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import io.mockk.every
import io.mockk.mockk

internal object MockFetchCredential {
    val credentialConfig = mockk<VcSdJwtCredentialConfiguration> {
        every { identifier } returns CREDENTIAL_IDENTIFIER
    }
    private val credentialConfig2 = mockk<VcSdJwtCredentialConfiguration> {
        every { identifier } returns CREDENTIAL_IDENTIFIER_2
    }

    val oneConfigCredentialInformation = mockk<IssuerCredentialInformation> {
        every { credentialEndpoint } returns CREDENTIAL_ENDPOINT
        every { credentialConfigurations } returns listOf(credentialConfig)
    }

    val multipleConfigCredentialInformation = mockk<IssuerCredentialInformation> {
        every { credentialEndpoint } returns CREDENTIAL_ENDPOINT
        every { credentialConfigurations } returns listOf(credentialConfig, credentialConfig2)
    }

    val noConfigCredentialInformation = mockk<IssuerCredentialInformation> {
        every { credentialEndpoint } returns CREDENTIAL_ENDPOINT
        every { credentialConfigurations } returns emptyList()
    }

    val oneIdentifierCredentialOffer = mockk<CredentialOffer> {
        every { credentialIssuer } returns CREDENTIAL_ISSUER
        every { credentialConfigurationIds } returns listOf(CREDENTIAL_IDENTIFIER)
    }

    val multipleIdentifiersCredentialOffer = mockk<CredentialOffer> {
        every { credentialIssuer } returns CREDENTIAL_ISSUER
        every { credentialConfigurationIds } returns listOf(CREDENTIAL_IDENTIFIER, CREDENTIAL_IDENTIFIER_2)
    }

    val noMatchingIdentifierCredentialOffer = mockk<CredentialOffer> {
        every { credentialIssuer } returns CREDENTIAL_ISSUER
        every { credentialConfigurationIds } returns listOf("otherCredentialIdentifier")
    }

    val noIdentifierCredentialOffer = mockk<CredentialOffer> {
        every { credentialIssuer } returns CREDENTIAL_ISSUER
        every { credentialConfigurationIds } returns emptyList()
    }

    const val CREDENTIAL_IDENTIFIER = "credentialIdentifier"
    const val CREDENTIAL_IDENTIFIER_2 = "credentialIdentifier2"
    const val CREDENTIAL_ISSUER = "credentialIssuer"
    const val CREDENTIAL_ENDPOINT = "credentialEndpoint"
}
