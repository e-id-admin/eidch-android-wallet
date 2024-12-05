package ch.admin.foitt.wallet.platform.credential.presentation.adapter

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState

fun interface GetCredentialCardState {
    suspend operator fun invoke(
        credentialPreview: CredentialPreview,
    ): CredentialCardState
}
