package ch.admin.foitt.wallet.feature.credentialDetail.presentation.model

import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus

data class CredentialDetailUiState(
    val credential: CredentialCardState,
    val claims: List<CredentialClaimData>,
    val issuer: ActorUiState,
) {
    companion object {
        val EMPTY = CredentialDetailUiState(
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
            issuer = ActorUiState("", null, TrustStatus.UNKNOWN),
        )
    }
}
