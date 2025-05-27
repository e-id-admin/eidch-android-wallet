package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun MediumCredentialBox(
    credentialCardState: CredentialCardState,
    modifier: Modifier = Modifier,
) = Column {
    WalletTexts.BodyLarge(
        text = stringResource(id = R.string.tk_present_review_credential_section_primary),
        modifier = modifier.padding(start = Sizes.s04, end = Sizes.s04, bottom = Sizes.s04)
    )
    Box(
        modifier = Modifier
            .clip(WalletTheme.shapes.large)
            .align(Alignment.CenterHorizontally)
            .background(WalletTheme.colorScheme.surfaceContainerLow),
    ) {
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
            CredentialIssuerPortrait(
                credentialState = credentialCardState,
            )
        } else {
            CredentialIssuerLandscape(
                credentialState = credentialCardState,
            )
        }
    }
}

@Composable
private fun CredentialIssuerPortrait(
    credentialState: CredentialCardState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(start = Sizes.s04, top = Sizes.s12, end = Sizes.s06, bottom = Sizes.s12)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CredentialContent(credentialState)
    }
}

@Composable
private fun CredentialIssuerLandscape(
    credentialState: CredentialCardState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(start = Sizes.s04, top = Sizes.s12, end = Sizes.s06, bottom = Sizes.s12)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CredentialContent(credentialState)
        }
    }
}

@Composable
private fun CredentialContent(
    credentialState: CredentialCardState,
) {
    CredentialCardSmall(credentialState = credentialState)
    Spacer(modifier = Modifier.width(Sizes.s04))
    Column {
        credentialState.title?.let {
            WalletTexts.TitleMedium(
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
    }
}

private class MediumCredentialBoxPreviewParams : PreviewParameterProvider<ComposableWrapper<CredentialCardState>> {
    override val values = CredentialMocks.cardStates
}

@Composable
@WalletComponentPreview
private fun MediumCredentialBoxPreview(
    @PreviewParameter(MediumCredentialBoxPreviewParams::class) previewParam: ComposableWrapper<CredentialCardState>,
) {
    WalletTheme {
        MediumCredentialBox(
            credentialCardState = previewParam.value(),
        )
    }
}
