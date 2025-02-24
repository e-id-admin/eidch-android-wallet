package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay

internal object MockCredentialDisplay {
    const val CREDENTIAL_ID = 2L
    val CREDENTIAL_STATUS = CredentialDisplayStatus.Valid
    const val TITLE = "credentialTitle"
    const val SUBTITLE = "credentialSubtitle"
    const val BACKGROUND_COLOR = "credentialBackgroundColor"
    val credentialDisplay = CredentialDisplay(
        credentialId = CREDENTIAL_ID,
        locale = "locale",
        name = TITLE,
        description = SUBTITLE,
        logoUri = null,
        logoAltText = null,
        backgroundColor = BACKGROUND_COLOR,
    )
    val credentialDisplays = listOf(credentialDisplay)
    val expectedCredentialPreview = CredentialPreview(
        credentialId = CREDENTIAL_ID,
        title = TITLE,
        subtitle = SUBTITLE,
        status = CREDENTIAL_STATUS,
        logoUri = null,
        backgroundColor = BACKGROUND_COLOR,
        isCredentialFromBetaIssuer = false
    )
}
