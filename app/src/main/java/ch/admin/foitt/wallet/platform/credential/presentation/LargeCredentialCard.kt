package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialCardMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Gradients
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun LargeCredentialCard(
    modifier: Modifier = Modifier,
    contentPaddingValues: PaddingValues = PaddingValues(Sizes.s06),
    minHeight: Dp = 555.dp,
    credentialCardState: CredentialCardState,
) = Surface(
    modifier = modifier.heightIn(min = minHeight),
    shape = RoundedCornerShape(size = Sizes.credentialCardCorner),
    color = credentialCardState.backgroundColor,
    contentColor = Color.Unspecified,
) {
    val textColor = credentialCardState.textColor
    Column(
        modifier = Modifier
            .drawBehind {
                drawRect(brush = Gradients.diagonalCredentialBrush())
                drawRect(brush = Gradients.leftBottomRadialLargeCredentialBrush(size))
                drawRect(brush = Gradients.bottomCenterRadialCredentialBrush(size))
            }
            .padding(contentPaddingValues)
    ) {
        CredentialContent(credentialCardState, textColor)
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(Sizes.s04))
        CredentialStatusBadge(status = credentialCardState.status, textColor = credentialCardState.textColor)
    }
}

@Composable
private fun CredentialContent(
    credentialCardState: CredentialCardState,
    textColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        CredentialText(
            modifier = Modifier.weight(1f),
            credentialCardState = credentialCardState,
            textColor = textColor
        )
        credentialCardState.logo?.let {
            Spacer(modifier = Modifier.width(Sizes.s04))
            Icon(
                painter = it,
                contentDescription = null,
                modifier = Modifier.size(Sizes.credentialLargeIconSize),
                tint = Color.Unspecified,
            )
        }
    }
}

@Composable
private fun CredentialText(
    modifier: Modifier = Modifier,
    credentialCardState: CredentialCardState,
    textColor: Color,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        credentialCardState.title?.let {
            WalletTexts.LargeCredentialTitle(
                text = it,
                color = textColor,
            )
        }
        credentialCardState.subtitle?.let {
            WalletTexts.LargeCredentialSubtitle(
                text = it,
                color = textColor.copy(alpha = 0.7f),
            )
        }
    }
}

private class LargeCredentialCardPreviewParams :
    PreviewParameterProvider<ComposableWrapper<CredentialCardState>> {
    override val values = CredentialCardMocks.mocks
}

@Composable
@WalletComponentPreview
private fun LargeCredentialCardPreview(
    @PreviewParameter(LargeCredentialCardPreviewParams::class) state: ComposableWrapper<CredentialCardState>,
) {
    WalletTheme {
        LargeCredentialCard(
            credentialCardState = state.value(),
        )
    }
}