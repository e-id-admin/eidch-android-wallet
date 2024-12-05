package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.PassphraseValidationErrorToastFixed
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.presentation.PassphraseInputComponent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.FullscreenGradient
import ch.admin.foitt.wallet.platform.scaffold.presentation.WalletTopBar
import ch.admin.foitt.wallet.platform.scaffold.presentation.WalletTopBarViewModel
import ch.admin.foitt.wallet.platform.scaffold.presentation.preview.getPreviewWalletTopBarViewModel
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTextFieldColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun OnboardingPassphraseScreen(
    viewModel: OnboardingPassphraseViewModel,
) {
    OnboardingPassphraseScreenContent(
        passphrase = viewModel.passphrase.collectAsStateWithLifecycle().value,
        passphraseInputFieldState = viewModel.passphraseInputFieldState.collectAsStateWithLifecycle().value,
        showPassphraseErrorToast = viewModel.showPassphraseErrorToast.collectAsStateWithLifecycle().value,
        isValidating = viewModel.isValidating.collectAsStateWithLifecycle().value,
        onUpdatePassphrase = viewModel::onUpdatePassphrase,
        onCheckPassphrase = viewModel::onCheckPassphrase,
        onClosePassphraseError = viewModel::onClosePassphraseError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingPassphraseScreenContent(
    walletTopBarViewModel: WalletTopBarViewModel = hiltViewModel(),
    passphrase: String,
    passphraseInputFieldState: PassphraseInputFieldState,
    showPassphraseErrorToast: Boolean,
    isValidating: Boolean,
    onUpdatePassphrase: (String) -> Unit,
    onCheckPassphrase: () -> Unit,
    onClosePassphraseError: () -> Unit,
) {
    FullscreenGradient()

    when (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> WalletLayouts.CompactContainerFloatingBottom(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = {
                WalletTopBar(
                    viewModel = walletTopBarViewModel,
                    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                )
            },
            verticalArrangement = Arrangement.Top,
            content = {
                CompactContent(
                    passphrase = passphrase,
                    passphraseInputFieldState = passphraseInputFieldState,
                    onUpdatePassphrase = onUpdatePassphrase,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
            auxiliaryContent = {
                AuxiliaryContent(
                    compactLayout = true,
                    passphraseInputFieldState = passphraseInputFieldState,
                    showPassphraseErrorToast = showPassphraseErrorToast,
                    isValidating = isValidating,
                    onClosePassphraseError = onClosePassphraseError,
                )
            },
            stickyBottomHorizontalAlignment = Alignment.End,
            stickyBottomContent = {
                BottomButton(
                    onCheckPassphrase = onCheckPassphrase,
                )
            },
        )

        else -> WalletLayouts.LargeContainerFloatingBottom(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = {
                WalletTopBar(
                    viewModel = walletTopBarViewModel,
                    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                )
            },
            useStatusBarPadding = false,
            verticalArrangement = Arrangement.Top,
            content = {
                LargeContent(
                    passphrase = passphrase,
                    passphraseInputFieldState = passphraseInputFieldState,
                    onUpdatePassphrase = onUpdatePassphrase,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
            auxiliaryContent = {
                AuxiliaryContent(
                    compactLayout = false,
                    passphraseInputFieldState = passphraseInputFieldState,
                    showPassphraseErrorToast = showPassphraseErrorToast,
                    isValidating = isValidating,
                    onClosePassphraseError = onClosePassphraseError,
                )
            },
        )
    }
}

@Composable
private fun CompactContent(
    passphrase: String,
    passphraseInputFieldState: PassphraseInputFieldState,
    onUpdatePassphrase: (String) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s12))
    PassphraseInputComponent(
        modifier = Modifier.fillMaxWidth(),
        passphraseInputFieldState = passphraseInputFieldState,
        passphrase = passphrase,
        colors = WalletTextFieldColors.textFieldColorsFixed(),
        placeholder = {
            Placeholder()
        },
        keyboardImeAction = ImeAction.Next,
        onKeyboardAction = onCheckPassphrase,
        onPassphraseChange = onUpdatePassphrase,
        onAnimationFinished = {},
    )
}

@Composable
private fun LargeContent(
    passphrase: String,
    passphraseInputFieldState: PassphraseInputFieldState,
    onUpdatePassphrase: (String) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        PassphraseInputComponent(
            modifier = Modifier.weight(1f),
            passphraseInputFieldState = passphraseInputFieldState,
            passphrase = passphrase,
            colors = WalletTextFieldColors.textFieldColorsFixed(),
            placeholder = {
                Placeholder()
            },
            keyboardImeAction = ImeAction.Next,
            onKeyboardAction = onCheckPassphrase,
            onPassphraseChange = onUpdatePassphrase,
            onAnimationFinished = {},
        )
        Spacer(modifier = Modifier.width(Sizes.s08))
        BottomButton(
            onCheckPassphrase = onCheckPassphrase
        )
    }
}

@Composable
private fun Placeholder() = WalletTexts.BodyLarge(
    text = stringResource(R.string.tk_onboarding_characters_note),
    color = WalletTheme.colorScheme.onSurfaceVariantFixed
)

@Composable
private fun AuxiliaryContent(
    compactLayout: Boolean,
    passphraseInputFieldState: PassphraseInputFieldState,
    showPassphraseErrorToast: Boolean,
    isValidating: Boolean,
    onClosePassphraseError: () -> Unit,
) {
    if (passphraseInputFieldState == PassphraseInputFieldState.Error && showPassphraseErrorToast && !isValidating) {
        PassphraseValidationErrorToastFixed(
            modifier = Modifier
                .padding(start = Sizes.s08, end = Sizes.s08, bottom = if (compactLayout) Sizes.s06 else Sizes.s04),
            text = R.string.tk_onboarding_passwordlength_notification,
            onIconEnd = onClosePassphraseError,
        )
    }
}

@Composable
private fun BottomButton(
    onCheckPassphrase: () -> Unit
) = Buttons.FilledPrimaryFixed(
    modifier = Modifier.testTag(TestTags.CONTINUE_BUTTON.name),
    text = stringResource(R.string.tk_global_continue),
    onClick = onCheckPassphrase
)

@WalletAllScreenPreview
@Composable
private fun OnboardingPassphraseScreenPreview() {
    WalletTheme {
        OnboardingPassphraseScreenContent(
            walletTopBarViewModel = getPreviewWalletTopBarViewModel(R.string.tk_global_enterpassword),
            passphrase = "abc123",
            passphraseInputFieldState = PassphraseInputFieldState.Error,
            showPassphraseErrorToast = true,
            isValidating = false,
            onUpdatePassphrase = {},
            onCheckPassphrase = {},
            onClosePassphraseError = {},
        )
    }
}
