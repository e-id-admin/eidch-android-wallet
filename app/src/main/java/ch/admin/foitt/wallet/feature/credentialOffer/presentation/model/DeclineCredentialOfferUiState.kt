package ch.admin.foitt.wallet.feature.credentialOffer.presentation.model

import ch.admin.foitt.wallet.platform.credential.presentation.model.IssuerUiState

data class DeclineCredentialOfferUiState(
    val issuer: IssuerUiState,
) {
    companion object {
        val EMPTY = DeclineCredentialOfferUiState(
            issuer = IssuerUiState(
                name = "",
                painter = null,
            ),
        )
    }
}
