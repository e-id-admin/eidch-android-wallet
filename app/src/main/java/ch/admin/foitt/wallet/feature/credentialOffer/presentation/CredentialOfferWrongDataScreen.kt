package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun CredentialOfferWrongDataScreen() {
    CredentialOfferWrongDataScreenContent()
}

@Composable
private fun CredentialOfferWrongDataScreenContent() = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_cross_circle_colored,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerHigh
        )
    },
    stickyBottomContent = null
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleLarge(
        text = stringResource(id = R.string.tk_receive_credentialOffer_wrongData_primary),
    )
    Spacer(modifier = Modifier.height(Sizes.s05))
    WalletTexts.BodyLarge(
        text = stringResource(id = R.string.tk_receive_credentialOffer_wrongData_secondary),
        modifier = Modifier.fillMaxWidth(),
    )
}

@WalletAllScreenPreview
@Composable
private fun CredentialOfferWrongDataScreenPreview() {
    WalletTheme {
        CredentialOfferWrongDataScreenContent()
    }
}
