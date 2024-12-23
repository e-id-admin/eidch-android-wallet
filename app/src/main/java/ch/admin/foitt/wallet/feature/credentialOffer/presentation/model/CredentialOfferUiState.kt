package ch.admin.foitt.wallet.feature.credentialOffer.presentation.model

import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData

data class CredentialOfferUiState(
    val issuer: ActorUiState,
    val credential: CredentialCardState,
    val claims: List<CredentialClaimData>,
) {
    companion object {
        val EMPTY = CredentialOfferUiState(
            issuer = ActorUiState.EMPTY,
            credential = CredentialCardState(
                credentialId = -1,
                status = CredentialStatus.UNKNOWN,
                title = "",
                subtitle = null,
                logo = null,
                backgroundColor = Color.Transparent,
                textColor = Color.Transparent,
                borderColor = Color.Transparent,
                isCredentialFromBetaIssuer = false
            ),
            claims = emptyList(),
        )
    }
}
