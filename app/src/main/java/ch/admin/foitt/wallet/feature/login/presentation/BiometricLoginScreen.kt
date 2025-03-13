package ch.admin.foitt.wallet.feature.login.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.centerHorizontallyOnFullscreen
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.FullscreenGradient
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    style = LoginNavAnimation::class,
)
@Composable
fun BiometricLoginScreen(
    viewModel: BiometricLoginViewModel
) {
    val currentActivity = LocalActivity.current

    OnResumeEventHandler {
        viewModel.tryLoginWithBiometric(currentActivity)
    }

    BackHandler {
        currentActivity.finish()
    }

    BiometricLoginScreenContent(
        showBiometricsLoginButton = viewModel.showBiometricLoginButton.collectAsStateWithLifecycle().value,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        onLoginWithBiometrics = { viewModel.tryLoginWithBiometric(currentActivity) },
        onLoginWithPassphrase = viewModel::navigateToLoginWithPassphrase,
    )
}

@Composable
fun BiometricLoginScreenContent(
    showBiometricsLoginButton: Boolean,
    isLoading: Boolean,
    onLoginWithBiometrics: () -> Unit,
    onLoginWithPassphrase: () -> Unit,
) {
    FullscreenGradient()
    when (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> WalletLayouts.CompactContainerFloatingBottom(
            content = {
                Content()
            },
            stickyBottomContent = {
                BottomButtons(
                    showBiometricsLoginButton = showBiometricsLoginButton,
                    onLoginWithBiometrics = onLoginWithBiometrics,
                    onLoginWithPassphrase = onLoginWithPassphrase,
                )
            },
        )
        else -> WalletLayouts.LargeContainerFloatingBottom(
            content = {
                Content()
            },
            stickyBottomContent = {
                BottomButtons(
                    showBiometricsLoginButton = showBiometricsLoginButton,
                    onLoginWithBiometrics = onLoginWithBiometrics,
                    onLoginWithPassphrase = onLoginWithPassphrase,
                )
            }
        )
    }

    LoadingOverlay(
        showOverlay = isLoading,
        color = WalletTheme.colorScheme.primaryFixed
    )
}

@Composable
private fun Content() = Column(
    modifier = Modifier.centerHorizontallyOnFullscreen(),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Icon(
        painter = painterResource(R.drawable.wallet_ic_dotted_cross),
        tint = WalletTheme.colorScheme.onGradientFixed,
        contentDescription = null,
    )
    Spacer(modifier = Modifier.height(Sizes.s04))
    WalletTexts.TitleLarge(
        text = stringResource(R.string.tk_global_welcomeback),
        textAlign = TextAlign.Center,
        color = WalletTheme.colorScheme.onGradientFixed
    )
    Spacer(modifier = Modifier.height(Sizes.s02))
    WalletTexts.TitleSmall(
        text = stringResource(R.string.tk_login_variant_body),
        textAlign = TextAlign.Center,
        color = WalletTheme.colorScheme.onGradientFixed
    )
}

@Composable
private fun BottomButtons(
    showBiometricsLoginButton: Boolean,
    onLoginWithBiometrics: () -> Unit,
    onLoginWithPassphrase: () -> Unit,
) {
    if (showBiometricsLoginButton) {
        Buttons.FilledPrimaryFixed(
            text = stringResource(R.string.tk_global_loginbiometric_primarybutton),
            startIcon = painterResource(R.drawable.ic_fingerprint),
            onClick = onLoginWithBiometrics,
        )
        Spacer(modifier = Modifier.size(Sizes.s04))
    }
    Buttons.FilledSecondaryFixed(
        text = stringResource(R.string.tk_global_loginpassword_secondarybutton),
        onClick = onLoginWithPassphrase,
    )
}

@WalletAllScreenPreview
@Composable
fun BiometricLoginScreenPreview() {
    WalletTheme {
        BiometricLoginScreenContent(
            showBiometricsLoginButton = true,
            isLoading = false,
            onLoginWithBiometrics = {},
            onLoginWithPassphrase = {},
        )
    }
}
