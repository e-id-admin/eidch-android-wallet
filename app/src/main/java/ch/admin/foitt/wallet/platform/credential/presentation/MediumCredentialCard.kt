package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import ch.admin.foitt.wallet.platform.composables.MediumCredentialCardLayout
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialCardMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Gradients
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun MediumCredentialCard(
    modifier: Modifier = Modifier,
    credentialCardState: CredentialCardState,
    isScrollingEnabled: Boolean = false,
    onClick: (() -> Unit)? = null,
) = Surface(
    modifier = modifier,
    shape = RoundedCornerShape(size = Sizes.credentialCardCorner),
    color = credentialCardState.backgroundColor,
    contentColor = Color.Unspecified,
) {
    BoxWithConstraints {
        // calculate height based on the ratio but max 500dp
        val preferredHeight = min(maxWidth.div(0.68f), 500.dp)
        val textColor = credentialCardState.textColor
        MediumCredentialCardLayout(
            modifier = Modifier
                .then(
                    onClick?.let {
                        Modifier
                            .clickable(onClick = onClick)
                            .spaceBarKeyClickable(onClick)
                    } ?: Modifier
                )
                .widthIn(max = 340.dp)
                .drawBehind {
                    drawRect(brush = Gradients.diagonalCredentialBrush())
                    drawRect(brush = Gradients.leftBottomRadialCredentialBrush(size))
                }
                .then(if (isScrollingEnabled) Modifier.verticalScroll(rememberScrollState()) else Modifier),
            padding = PaddingValues(Sizes.s04),
            preferredHeight = min(maxHeight, preferredHeight),
            icon = credentialCardState.logo?.let { logo ->
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Sizes.s03),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Icon(
                            painter = logo,
                            contentDescription = null,
                            modifier = Modifier
                                .size(Sizes.credentialIconSize)
                                .focusable(false),
                            tint = Color.Unspecified,
                        )
                    }
                }
            },
            title = credentialCardState.title?.let {
                {
                    WalletTexts.MediumCredentialTitle(
                        text = it,
                        color = textColor,
                    )
                }
            },
            subtitle =
            credentialCardState.subtitle?.let {
                {
                    WalletTexts.MediumCredentialSubtitle(
                        text = it,
                        color = textColor.copy(alpha = 0.7f),
                    )
                }
            },
        )
    }
}

private class MediumCredentialCardPreviewParams :
    PreviewParameterProvider<ComposableWrapper<CredentialCardState>> {
    override val values = CredentialCardMocks.mocks
}

@Composable
@WalletComponentPreview
private fun MediumCredentialCardPreview(
    @PreviewParameter(MediumCredentialCardPreviewParams::class) state: ComposableWrapper<CredentialCardState>,
) {
    WalletTheme {
        MediumCredentialCard(
            credentialCardState = state.value(),
        )
    }
}
