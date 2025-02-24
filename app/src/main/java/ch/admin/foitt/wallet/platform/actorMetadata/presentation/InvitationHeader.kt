package ch.admin.foitt.wallet.platform.actorMetadata.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Avatar
import ch.admin.foitt.wallet.platform.composables.AvatarSize
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun InvitationHeader(
    inviterName: String,
    inviterImage: Painter?,
    trustStatus: TrustStatus,
    modifier: Modifier = Modifier,
) = Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
) {
    Avatar(
        imagePainter = inviterImage,
        size = AvatarSize.LARGE,
        imageTint = WalletTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.size(Sizes.s04))
    Column(
        modifier = Modifier
            .weight(1f)
            .semantics(mergeDescendants = true) {}
    ) {
        WalletTexts.TitleMedium(
            text = inviterName,
            color = WalletTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrustIcon(trustStatus)
            Spacer(Modifier.width(Sizes.s01))
            TrustLabel(trustStatus)
        }
    }
}

@Composable
private fun RowScope.TrustIcon(trustStatus: TrustStatus) = when (trustStatus) {
    TrustStatus.TRUSTED -> Icon(
        painter = painterResource(R.drawable.wallet_ic_trusted),
        contentDescription = null,
        tint = WalletTheme.colorScheme.tertiary,
        modifier = Modifier.size(Sizes.s04)
    )
    TrustStatus.NOT_TRUSTED -> Icon(
        painter = painterResource(R.drawable.wallet_ic_not_trusted),
        contentDescription = null,
        tint = WalletTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(Sizes.s04)
    )
    else -> {}
}

@Composable
private fun RowScope.TrustLabel(trustStatus: TrustStatus) = when (trustStatus) {
    TrustStatus.TRUSTED -> WalletTexts.LabelLarge(
        text = stringResource(R.string.tk_global_trust_status_trusted),
        color = WalletTheme.colorScheme.tertiary,
        modifier = Modifier.weight(1f)
    )
    TrustStatus.NOT_TRUSTED -> WalletTexts.LabelLarge(
        text = stringResource(R.string.tk_global_trust_status_notTrusted),
        color = WalletTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(1f)
    )
    else -> {}
}

private class InvitationHeaderPreviewParams : PreviewParameterProvider<Triple<String, Int, TrustStatus>> {
    override val values: Sequence<Triple<String, Int, TrustStatus>> = sequenceOf(
        Triple("Issuer Name", R.drawable.wallet_ic_eid, TrustStatus.TRUSTED),
        Triple("Issuer with a veeeeryyyyy loooonnnnnng name", R.drawable.ic_launcher_background, TrustStatus.TRUSTED),
        Triple("Issuer Name not trusted", R.drawable.wallet_ic_eid, TrustStatus.NOT_TRUSTED),
        Triple("Issuer Name trust unknown", R.drawable.wallet_ic_dotted_cross, TrustStatus.UNKNOWN),
    )
}

@WalletComponentPreview
@Composable
private fun InvitationHeaderPreview(
    @PreviewParameter(InvitationHeaderPreviewParams::class) previewParams: Triple<String, Int, TrustStatus>,
) {
    WalletTheme {
        InvitationHeader(
            inviterName = previewParams.first,
            inviterImage = painterResource(previewParams.second),
            trustStatus = previewParams.third,
        )
    }
}
