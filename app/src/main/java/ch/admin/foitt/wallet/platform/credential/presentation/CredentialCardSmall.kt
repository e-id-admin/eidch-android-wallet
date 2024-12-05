package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Gradients
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun CredentialCardSmall(
    credentialState: CredentialCardState,
    modifier: Modifier = Modifier,
) = Surface(
    modifier = modifier
        .width(Sizes.credentialSmallWidth)
        .height(Sizes.credentialSmallHeight),
    shape = RoundedCornerShape(size = Sizes.s03),
    color = credentialState.backgroundColor,
    contentColor = Color.Unspecified,
) {
    Box(
        modifier = Modifier
            .drawBehind {
                drawRect(brush = Gradients.diagonalCredentialBrush())
            }
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        credentialState.logo?.let { logo ->
            Icon(
                painter = logo,
                contentDescription = null,
                modifier = Modifier.size(Sizes.credentialSmallIconSize),
                tint = Color.Unspecified,
            )
        }
    }
}

private class CredentialCardSmallPreviewParams :
    PreviewParameterProvider<ComposableWrapper<CredentialCardState>> {
    override val values = CredentialMocks.cardStates
}

@WalletComponentPreview
@Composable
private fun CredentialCardSmallPreview(
    @PreviewParameter(CredentialCardSmallPreviewParams::class) state: ComposableWrapper<CredentialCardState>,
) {
    WalletTheme {
        CredentialCardSmall(
            credentialState = state.value(),
        )
    }
}
