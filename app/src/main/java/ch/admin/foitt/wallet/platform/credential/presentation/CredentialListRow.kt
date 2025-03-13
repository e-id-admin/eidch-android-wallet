package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun CredentialListRow(
    credentialState: CredentialCardState,
    showDivider: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .spaceBarKeyClickable(onClick)
            .padding(start = Sizes.s04, top = Sizes.s03, end = Sizes.s06, bottom = Sizes.s03),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CredentialCardSmall(credentialState = credentialState)
        Spacer(modifier = Modifier.width(Sizes.s04))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            credentialState.title?.let {
                WalletTexts.BodyLarge(
                    text = credentialState.title,
                    color = WalletTheme.colorScheme.onSurface,
                )
            }
            credentialState.subtitle?.let {
                WalletTexts.BodyLarge(
                    text = credentialState.subtitle,
                    color = WalletTheme.colorScheme.onSurfaceVariant,
                )
            }
            credentialState.status?.let {
                CredentialStatus(
                    status = credentialState.status,
                    isCredentialFromBetaIssuer = credentialState.isCredentialFromBetaIssuer,
                )
            }
        }
        Spacer(modifier = Modifier.width(Sizes.s04))
        Icon(
            modifier = Modifier.size(Sizes.s06),
            painter = painterResource(id = R.drawable.wallet_ic_chevron),
            contentDescription = null,
            tint = WalletTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(start = Sizes.s04)
        )
    }
}

@Composable
private fun CredentialStatus(
    status: CredentialDisplayStatus,
    isCredentialFromBetaIssuer: Boolean,
) {
    val contentColor = when (status) {
        CredentialDisplayStatus.Valid,
        CredentialDisplayStatus.Unsupported,
        CredentialDisplayStatus.Unknown -> WalletTheme.colorScheme.onSurfaceVariant
        is CredentialDisplayStatus.NotYetValid,
        is CredentialDisplayStatus.Expired,
        CredentialDisplayStatus.Revoked,
        CredentialDisplayStatus.Suspended -> WalletTheme.colorScheme.error
    }
    val bodyTextHeight = with(LocalDensity.current) {
        WalletTheme.typography.bodyMedium.lineHeight.toDp()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isCredentialFromBetaIssuer) {
            DemoBadge(
                textColor = WalletTheme.colorScheme.onLightPrimary,
                backgroundColor = WalletTheme.colorScheme.lightPrimary,
            )
            Spacer(modifier = Modifier.width(Sizes.s02))
        }
        Icon(
            painter = painterResource(id = status.getIcon()),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.sizeIn(
                maxWidth = bodyTextHeight,
                maxHeight = bodyTextHeight,
            )
        )
        Spacer(modifier = Modifier.size(Sizes.s01))
        WalletTexts.Body(
            text = status.getText(),
            color = contentColor,
        )
    }
}

private class CredentialListRowPreviewParams : PreviewParameterProvider<ComposableWrapper<CredentialCardState>> {
    override val values = CredentialMocks.cardStates
}

@WalletComponentPreview
@Composable
private fun CredentialListRowPreview(
    @PreviewParameter(CredentialListRowPreviewParams::class) state: ComposableWrapper<CredentialCardState>,
) {
    WalletTheme {
        CredentialListRow(
            credentialState = state.value(),
            showDivider = true,
            onClick = {},
        )
    }
}
