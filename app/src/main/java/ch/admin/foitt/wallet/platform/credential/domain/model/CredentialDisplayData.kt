package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay

data class CredentialDisplayData(
    val credentialId: Long,
    val title: String,
    val subtitle: String?,
    val status: CredentialDisplayStatus,
    val logoUri: String?,
    val backgroundColor: String?,
    val isCredentialFromBetaIssuer: Boolean
) {
    constructor(
        credentialId: Long,
        status: CredentialDisplayStatus,
        credentialDisplay: CredentialDisplay,
        isCredentialFromBetaIssuer: Boolean
    ) : this(
        credentialId = credentialId,
        status = status,
        title = credentialDisplay.name,
        subtitle = credentialDisplay.description,
        logoUri = credentialDisplay.logoUri,
        backgroundColor = credentialDisplay.backgroundColor,
        isCredentialFromBetaIssuer = isCredentialFromBetaIssuer
    )
}
