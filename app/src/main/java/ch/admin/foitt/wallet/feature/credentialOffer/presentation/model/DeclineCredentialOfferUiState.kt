package ch.admin.foitt.wallet.feature.credentialOffer.presentation.model

import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus

data class DeclineCredentialOfferUiState(
    val issuer: ActorUiState,
) {
    companion object {
        val EMPTY = DeclineCredentialOfferUiState(
            issuer = ActorUiState(
                name = "",
                painter = null,
                trustStatus = TrustStatus.UNKNOWN,
            ),
        )
    }
}
