package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.zIndex
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Gradients
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun CredentialHalfCard(
    credentialCardState: CredentialCardState,
    modifier: Modifier = Modifier,
) = Box(modifier = Modifier.fillMaxWidth()) {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f),
        thickness = Sizes.line01,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Sizes.s04, end = Sizes.s04, bottom = Sizes.s02)
            .height(Sizes.credentialCardSmallHeight),
        shape = RoundedCornerShape(bottomStart = Sizes.s02, bottomEnd = Sizes.s02),
        color = credentialCardState.backgroundColor,
        contentColor = Color.Unspecified,
        border = BorderStroke(
            width = Sizes.line01,
            color = credentialCardState.backgroundColor,
        ),
        shadowElevation = Sizes.s01,
    ) {
        Column(
            Modifier
                .background(brush = Gradients.diagonalCredentialBrush())
                .fillMaxSize()
                .padding(Sizes.s04)
        ) {
            CredentialCardContent(
                credentialCardState = credentialCardState,
                modifier = Modifier
            )
        }
    }
}

private class CredentialHalfCardPreviewParams : PreviewParameterProvider<ComposableWrapper<CredentialCardState>> {
    override val values = CredentialMocks.cardStates
}

@Composable
@WalletComponentPreview
private fun CredentialCardHalfPreview(
    @PreviewParameter(CredentialHalfCardPreviewParams::class) previewParam: ComposableWrapper<CredentialCardState>,
) {
    WalletTheme {
        CredentialHalfCard(
            credentialCardState = previewParam.value(),
        )
    }
}
