package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus

data class CredentialPreview(
    val credentialId: Long,
    val title: String,
    val subtitle: String?,
    val status: CredentialStatus,
    val logoUri: String?,
    val backgroundColor: String?,
    val isCredentialFromBetaIssuer: Boolean
) {
    constructor(
        credential: Credential,
        credentialDisplay: CredentialDisplay,
        isCredentialFromBetaIssuer: Boolean
    ) : this (
        credentialId = credential.id,
        status = credential.status,
        title = credentialDisplay.name,
        subtitle = credentialDisplay.description,
        logoUri = credentialDisplay.logoUri,
        backgroundColor = credentialDisplay.backgroundColor,
        isCredentialFromBetaIssuer = isCredentialFromBetaIssuer
    )
}
