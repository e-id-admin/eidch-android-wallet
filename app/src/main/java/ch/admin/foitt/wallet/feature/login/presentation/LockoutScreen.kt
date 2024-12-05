package ch.admin.foitt.wallet.feature.login.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
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
import ch.admin.foitt.wallet.theme.WalletButtonColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun LockoutScreen(viewModel: LockoutViewModel) {
    val currentActivity = LocalActivity.current
    val keyboardController = LocalSoftwareKeyboardController.current

    SideEffect {
        keyboardController?.hide()
    }

    BackHandler {
        currentActivity.finish()
    }

    OnResumeEventHandler {
        viewModel.canUseBiometrics()
        viewModel.checkLockoutDuration()
    }

    LockoutScreenContent(
        countdown = viewModel.countdown.collectAsStateWithLifecycle().value,
        showBiometricLoginButton = viewModel.showBiometricLoginButton.collectAsStateWithLifecycle().value,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        onLoginWithBiometrics = { viewModel.tryLoginWithBiometrics(currentActivity) },
        onPassphraseForgotten = viewModel::onPassphraseForgotten,
    )
}

@Composable
private fun LockoutScreenContent(
    countdown: Pair<Long, LockoutViewModel.TimeUnit>,
    showBiometricLoginButton: Boolean,
    isLoading: Boolean,
    onLoginWithBiometrics: () -> Unit,
    onPassphraseForgotten: () -> Unit,
) {
    FullscreenGradient()
    when (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> WalletLayouts.CompactContainerFloatingBottom(
            modifier = Modifier.fillMaxSize(),
            content = {
                Content(
                    countdown = countdown,
                )
            },
            stickyBottomContent = {
                BottomButtons(
                    showBiometricLoginButton = showBiometricLoginButton,
                    onLoginWithBiometrics = onLoginWithBiometrics,
                    onPassphraseForgotten = onPassphraseForgotten,
                )
            }
        )
        else -> WalletLayouts.LargeContainerFloatingBottom(
            modifier = Modifier.fillMaxSize(),
            content = {
                Content(
                    countdown = countdown,
                    showIcon = false,
                )
            },
            stickyBottomContent = {
                BottomButtons(
                    showBiometricLoginButton = showBiometricLoginButton,
                    onLoginWithBiometrics = onLoginWithBiometrics,
                    onPassphraseForgotten = onPassphraseForgotten,
                )
            }
        )
    }

    LoadingOverlay(
        showOverlay = isLoading,
        color = WalletTheme.colorScheme.primaryFixed,
    )
}

@Composable
private fun Content(
    countdown: Pair<Long, LockoutViewModel.TimeUnit>,
    showIcon: Boolean = true,
) = Column(
    modifier = Modifier.centerHorizontallyOnFullscreen(),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    if (showIcon) {
        Icon(
            modifier = Modifier.size(Sizes.s14),
            painter = painterResource(R.drawable.wallet_ic_lock),
            tint = WalletTheme.colorScheme.onGradientFixed,
            contentDescription = null,
        )
    }
    Spacer(modifier = Modifier.height(Sizes.s04))
    WalletTexts.TitleLarge(
        text = stringResource(R.string.tk_login_locked_title),
        textAlign = TextAlign.Center,
        color = WalletTheme.colorScheme.onGradientFixed,
    )
    Spacer(modifier = Modifier.height(Sizes.s02))
    WalletTexts.TitleSmall(
        text = pluralStringResource(
            id = if (countdown.second == LockoutViewModel.TimeUnit.MINUTES) {
                R.plurals.tk_login_locked_body
            } else {
                R.plurals.tk_login_locked_seconds
            },
            count = countdown.first.toInt(),
            formatArgs = arrayOf(countdown.first),
        ),
        textAlign = TextAlign.Center,
        color = WalletTheme.colorScheme.onGradientFixed,
    )
}

@Composable
private fun BottomButtons(
    showBiometricLoginButton: Boolean,
    onLoginWithBiometrics: () -> Unit,
    onPassphraseForgotten: () -> Unit,
) {
    if (showBiometricLoginButton) {
        Buttons.FilledPrimaryFixed(
            text = stringResource(R.string.tk_global_loginbiometric_primarybutton),
            startIcon = painterResource(R.drawable.ic_fingerprint),
            onClick = onLoginWithBiometrics,
        )
        Spacer(modifier = Modifier.height(Sizes.s02))
    }
    Buttons.Text(
        text = stringResource(R.string.tk_login_locked_secondarybutton_text),
        colors = WalletButtonColors.text().copy(contentColor = WalletTheme.colorScheme.secondaryFixed),
        onClick = onPassphraseForgotten,
    )
}

@WalletAllScreenPreview
@Composable
private fun LockoutScreenPreview() {
    WalletTheme {
        LockoutScreenContent(
            countdown = Pair(3, LockoutViewModel.TimeUnit.SECONDS),
            showBiometricLoginButton = true,
            isLoading = false,
            onLoginWithBiometrics = {},
            onPassphraseForgotten = {},
        )
    }
}
