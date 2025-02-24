package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay

data class CredentialPreview(
    val credentialId: Long,
    val title: String,
    val subtitle: String?,
    val status: CredentialDisplayStatus,
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
        status = credential.getDisplayStatus(),
        title = credentialDisplay.name,
        subtitle = credentialDisplay.description,
        logoUri = credentialDisplay.logoUri,
        backgroundColor = credentialDisplay.backgroundColor,
        isCredentialFromBetaIssuer = isCredentialFromBetaIssuer
    )
}
