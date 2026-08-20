package ch.admin.foitt.wallet.feature.presentationRequest.presentation.model

import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.platform.badges.presentation.model.ClaimBadgeUiState
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster

data class PresentationRequestUiState(
    val credentialCardState: CredentialCardState,
    val requestedClaims: List<CredentialClaimCluster>,
    val claimBadgesUiStates: List<ClaimBadgeUiState>,
    val numberOfClaims: Int,
    val showsUnregisteredRequestCallout: Boolean,
) {
    companion object {
        val EMPTY by lazy {
            PresentationRequestUiState(
                credentialCardState = CredentialCardState(
                    credentialId = -1,
                    status = CredentialDisplayStatus.Unknown,
                    title = "",
                    subtitle = null,
                    logo = null,
                    backgroundColor = Color.Unspecified,
                    contentColor = Color.Unspecified,
                    borderColor = Color.Unspecified,
                    isCredentialFromBetaIssuer = false,
                ),
                requestedClaims = emptyList(),
                claimBadgesUiStates = emptyList(),
                numberOfClaims = 0,
                showsUnregisteredRequestCallout = true
            )
        }
    }
}
