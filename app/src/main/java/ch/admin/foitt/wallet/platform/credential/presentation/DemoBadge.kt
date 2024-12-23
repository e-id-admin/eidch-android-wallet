package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun DemoBadge() {
    val altText = stringResource(id = R.string.tk_global_credential_status_demo_alt)
    Box(
        modifier = Modifier
            .heightIn(min = Sizes.labelHeight)
            .clearAndSetSemantics {
                contentDescription = altText
            }
            .background(
                color = WalletTheme.colorScheme.secondaryFixed,
                shape = RoundedCornerShape(Sizes.s04)
            )
            .padding(start = Sizes.s03, end = Sizes.s04),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = R.string.tk_global_credential_status_demo),
            color = WalletTheme.colorScheme.onSecondaryFixed,
            style = WalletTheme.typography.labelMedium,
        )
    }
}

@Composable
@WalletComponentPreview
private fun CredentialStatusBadgePreview() {
    WalletTheme {
        DemoBadge()
    }
}
