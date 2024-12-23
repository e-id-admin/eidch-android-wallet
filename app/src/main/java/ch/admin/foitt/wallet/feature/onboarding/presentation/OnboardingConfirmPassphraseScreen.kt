package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.onboarding.presentation.composables.OnboardingLoadingScreenContent
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.PassphraseValidationErrorToastFixed
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.navArgs.domain.model.ConfirmPassphraseNavArg
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

@Destination(
    navArgsDelegate = ConfirmPassphraseNavArg::class,
)
@Composable
fun OnboardingConfirmPassphraseScreen(
    viewModel: OnboardingConfirmPassphraseViewModel,
) {
    OnboardingConfirmPassphraseScreenContent(
        textFieldValue = viewModel.textFieldValue.collectAsStateWithLifecycle().value,
        passphraseInputFieldState = viewModel.passphraseInputFieldState.collectAsStateWithLifecycle().value,
        showSupportText = viewModel.showSupportText.collectAsStateWithLifecycle().value,
        confirmationAttemptsLeft = viewModel.remainingConfirmationAttempts.collectAsStateWithLifecycle().value,
        showPassphraseErrorToast = viewModel.showPassphraseErrorToast.collectAsStateWithLifecycle().value,
        isInitializing = viewModel.isInitializing.collectAsStateWithLifecycle().value,
        onTextFieldValueChange = viewModel::onTextFieldValueChange,
        onCheckPassphrase = viewModel::onCheckPassphrase,
        onClosePassphraseError = viewModel::onClosePassphraseError,
    )
}

@Composable
private fun OnboardingConfirmPassphraseScreenContent(
    walletTopBarViewModel: WalletTopBarViewModel = hiltViewModel(),
    textFieldValue: TextFieldValue,
    confirmationAttemptsLeft: Int,
    passphraseInputFieldState: PassphraseInputFieldState,
    showSupportText: Boolean,
    showPassphraseErrorToast: Boolean,
    isInitializing: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
    onClosePassphraseError: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(isInitializing) {
        if (isInitializing) {
            keyboard?.hide()
        }
    }

    AnimatedContent(targetState = isInitializing, label = "loadingFadeIn") { initializing ->
        if (initializing) {
            OnboardingLoadingScreenContent()
        } else {
            OnboardingConfirmPassphraseContent(
                walletTopBarViewModel = walletTopBarViewModel,
                textFieldValue = textFieldValue,
                passphraseInputFieldState = passphraseInputFieldState,
                confirmationAttemptsLeft = confirmationAttemptsLeft,
                showSupportText = showSupportText,
                showPassphraseErrorToast = showPassphraseErrorToast,
                onTextFieldValueChange = onTextFieldValueChange,
                onCheckPassphrase = onCheckPassphrase,
                onClosePassphraseError = onClosePassphraseError,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingConfirmPassphraseContent(
    walletTopBarViewModel: WalletTopBarViewModel = hiltViewModel(),
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    confirmationAttemptsLeft: Int,
    showSupportText: Boolean,
    showPassphraseErrorToast: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
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
                    textFieldValue = textFieldValue,
                    passphraseInputFieldState = passphraseInputFieldState,
                    confirmationAttemptsLeft = confirmationAttemptsLeft,
                    showSupportText = showSupportText,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
            auxiliaryContent = {
                AuxiliaryContent(
                    compactLayout = true,
                    passphraseInputFieldState = passphraseInputFieldState,
                    showPassphraseErrorToast = showPassphraseErrorToast,
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
                    textFieldValue = textFieldValue,
                    passphraseInputFieldState = passphraseInputFieldState,
                    confirmationAttemptsLeft = confirmationAttemptsLeft,
                    showSupportText = showSupportText,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
            auxiliaryContent = {
                AuxiliaryContent(
                    compactLayout = false,
                    passphraseInputFieldState = passphraseInputFieldState,
                    showPassphraseErrorToast = showPassphraseErrorToast,
                    onClosePassphraseError = onClosePassphraseError,
                )
            },
        )
    }
}

@Composable
private fun CompactContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    confirmationAttemptsLeft: Int,
    showSupportText: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s12))
    PassphraseInputComponent(
        modifier = Modifier.fillMaxWidth(),
        passphraseInputFieldState = passphraseInputFieldState,
        textFieldValue = textFieldValue,
        colors = WalletTextFieldColors.textFieldColorsFixed(),
        placeholder = {
            Placeholder()
        },
        supportingText = {
            if (showSupportText) {
                SupportingText(
                    confirmationAttemptsLeft = confirmationAttemptsLeft,
                )
            }
        },
        keyboardImeAction = ImeAction.Next,
        onKeyboardAction = onCheckPassphrase,
        onTextFieldValueChange = onTextFieldValueChange,
        onAnimationFinished = {},
    )
}

@Composable
private fun LargeContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    confirmationAttemptsLeft: Int,
    showSupportText: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        PassphraseInputComponent(
            modifier = Modifier.weight(1f),
            passphraseInputFieldState = passphraseInputFieldState,
            textFieldValue = textFieldValue,
            colors = WalletTextFieldColors.textFieldColorsFixed(),
            placeholder = {
                Placeholder()
            },
            supportingText = {
                if (showSupportText) {
                    SupportingText(
                        confirmationAttemptsLeft = confirmationAttemptsLeft,
                    )
                }
            },
            keyboardImeAction = ImeAction.Next,
            onKeyboardAction = onCheckPassphrase,
            onTextFieldValueChange = onTextFieldValueChange,
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
    text = stringResource(R.string.tk_login_password_alt),
    color = WalletTheme.colorScheme.onSurfaceVariantFixed
)

@Composable
private fun SupportingText(
    confirmationAttemptsLeft: Int,
) = WalletTexts.BodySmall(
    modifier = Modifier,
    text = pluralStringResource(R.plurals.tk_global_try_android, confirmationAttemptsLeft, confirmationAttemptsLeft),
    color = WalletTheme.colorScheme.onGradientFixed
)

@Composable
private fun AuxiliaryContent(
    compactLayout: Boolean,
    passphraseInputFieldState: PassphraseInputFieldState,
    showPassphraseErrorToast: Boolean,
    onClosePassphraseError: () -> Unit,
) {
    if (passphraseInputFieldState == PassphraseInputFieldState.Error && showPassphraseErrorToast) {
        PassphraseValidationErrorToastFixed(
            modifier = Modifier
                .padding(start = Sizes.s08, end = Sizes.s08, bottom = if (compactLayout) Sizes.s06 else Sizes.s04),
            text = R.string.tk_onboarding_nopasswordmismatch_notification,
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
private fun OnboardingConfirmPassphraseScreenPreview() {
    WalletTheme {
        OnboardingConfirmPassphraseScreenContent(
            walletTopBarViewModel = getPreviewWalletTopBarViewModel(R.string.tk_global_confirmpassword),
            textFieldValue = TextFieldValue("abc123"),
            confirmationAttemptsLeft = 4,
            passphraseInputFieldState = PassphraseInputFieldState.Error,
            showSupportText = true,
            showPassphraseErrorToast = true,
            isInitializing = false,
            onTextFieldValueChange = {},
            onCheckPassphrase = {},
            onClosePassphraseError = {},
        )
    }
}
