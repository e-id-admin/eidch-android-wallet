package ch.admin.foitt.wallet.feature.credentialDetail.presentation.model

import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credential.presentation.model.IssuerUiState
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData

data class CredentialDetailUiState(
    val credential: CredentialCardState,
    val claims: List<CredentialClaimData>,
    val issuer: IssuerUiState,
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
            issuer = IssuerUiState("", null),
        )
    }
}
