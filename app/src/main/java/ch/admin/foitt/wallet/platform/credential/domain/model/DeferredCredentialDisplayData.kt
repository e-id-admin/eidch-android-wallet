package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState

data class DeferredCredentialDisplayData(
    val credentialId: Long,
    val title: String?,
    val status: DeferredProgressionState,
    val logoUri: String?,
    val backgroundColor: String?,
    val createdAt: Long,
) {
    constructor(
        credentialId: Long,
        status: DeferredProgressionState,
        credentialDisplay: CredentialDisplay,
        createdAt: Long,
    ) : this(
        credentialId = credentialId,
        title = credentialDisplay.name,
        status = status,
        logoUri = credentialDisplay.logoUri,
        backgroundColor = credentialDisplay.backgroundColor,
        createdAt = createdAt,
    )
}
