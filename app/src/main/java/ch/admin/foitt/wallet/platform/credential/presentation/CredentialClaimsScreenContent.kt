package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun CredentialClaimsScreenContent(
    modifier: Modifier = Modifier,
    topContent: @Composable LazyItemScope.() -> Unit = {},
    bottomContent: @Composable LazyItemScope.() -> Unit = {},
    @StringRes title: Int,
    claims: List<CredentialClaimData>,
    credentialCardState: CredentialCardState,
) = LazyColumn(
    modifier = modifier
        .fillMaxWidth()
) {
    item {
        topContent()
    }
    item {
        CredentialHalfCard(
            credentialCardState = credentialCardState,
        )
    }
    credentialClaimItems(title, claims, "issuer", null, {})
    item {
        bottomContent()
    }
}

@WalletComponentPreview
@Composable
private fun CredentialClaimsScreenContentPreview() {
    WalletTheme {
        CredentialClaimsScreenContent(
            topContent = { },
            bottomContent = { },
            title = R.string.presentation_attributes_title,
            claims = CredentialMocks.claimList,
            credentialCardState = CredentialMocks.cardState01,
        )
    }
}
