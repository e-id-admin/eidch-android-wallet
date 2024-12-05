package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.InvitationHeader
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.credential.presentation.model.IssuerUiState
import ch.admin.foitt.wallet.platform.navArgs.domain.model.CredentialOfferDeclinedNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    navArgsDelegate = CredentialOfferDeclinedNavArg::class,
)
@Composable
fun CredentialOfferDeclinedScreen(
    viewModel: CredentialOfferDeclinedViewModel,
) {
    BackHandler {
        viewModel.navigateToHome()
    }

    CredentialOfferDeclinedScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        issuer = viewModel.uiState.collectAsStateWithLifecycle().value.issuer,
    )
}

@Composable
private fun CredentialOfferDeclinedScreenContent(
    isLoading: Boolean,
    issuer: IssuerUiState,
) = Box(
    modifier = Modifier.fillMaxSize(),
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        InvitationHeader(
            modifier = Modifier.padding(top = Sizes.s01, start = Sizes.s06, end = Sizes.s06),
            inviterName = issuer.name,
            inviterImage = issuer.painter,
            message = stringResource(id = R.string.credential_offer_invitation),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        Sheet(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
    LoadingOverlay(showOverlay = isLoading)
}

@Composable
private fun Sheet(
    modifier: Modifier,
) = Box(
    modifier = modifier
        .clip(RoundedCornerShape(topStart = Sizes.boxCornerSize, topEnd = Sizes.boxCornerSize))
        .background(WalletTheme.colorScheme.primary)
        .padding(top = Sizes.s06, start = Sizes.s06, end = Sizes.s06),
    contentAlignment = Alignment.Center
) {
    Column(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.wallet_ic_circular_cross),
            contentDescription = null,
            tint = WalletTheme.colorScheme.lightPrimary,
        )
        Spacer(modifier = Modifier.height(Sizes.s01))
        WalletTexts.BodyLarge(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.tk_receive_deny2_title),
            color = WalletTheme.colorScheme.lightPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun CredentialOfferDeclinedScreenContentPreview() {
    WalletTheme {
        CredentialOfferDeclinedScreenContent(
            isLoading = false,
            issuer = IssuerUiState(
                name = "Test Issuer",
                painter = painterResource(id = R.drawable.wallet_ic_scan_person)
            ),
        )
    }
}
