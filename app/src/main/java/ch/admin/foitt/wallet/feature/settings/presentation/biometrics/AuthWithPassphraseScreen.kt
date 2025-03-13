package ch.admin.foitt.wallet.feature.settings.presentation.biometrics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.navArgs.domain.model.AuthWithPassphraseNavArg
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.presentation.PassphraseInputComponent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTextFieldColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    navArgsDelegate = AuthWithPassphraseNavArg::class,
)
@Composable
fun AuthWithPassphraseScreen(
    viewModel: AuthWithPassphraseViewModel
) {
    OnResumeEventHandler {
        viewModel.checkRemainingAttempts()
    }

    AuthWithPassphraseScreenContent(
        enableBiometrics = viewModel.enableBiometrics,
        passphraseInputFieldState = viewModel.passphraseInputFieldState.collectAsStateWithLifecycle().value,
        textFieldValue = viewModel.textFieldValue.collectAsStateWithLifecycle().value,
        initialSupportText = viewModel.initialSupportText.collectAsStateWithLifecycle().value,
        isPassphraseValid = viewModel.isPassphraseValid.collectAsStateWithLifecycle().value,
        remainingAuthAttempts = viewModel.remainingAuthAttempts.collectAsStateWithLifecycle().value,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        onTextFieldValueChange = viewModel::onTextFieldValueChange,
        onCheckPassphrase = viewModel::onCheckPassphrase,
    )
}

@Composable
private fun AuthWithPassphraseScreenContent(
    enableBiometrics: Boolean,
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    isPassphraseValid: Boolean,
    initialSupportText: Boolean,
    remainingAuthAttempts: Int,
    isLoading: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    when (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> WalletLayouts.CompactContainerFloatingBottom(
            shouldScrollUnderTopBar = false,
            verticalArrangement = Arrangement.Top,
            content = {
                CompactContent(
                    enableBiometrics = enableBiometrics,
                    textFieldValue = textFieldValue,
                    passphraseInputFieldState = passphraseInputFieldState,
                    initialSupportText = initialSupportText,
                    remainingAuthAttempts = remainingAuthAttempts,
                    isLoading = isLoading,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
            stickyBottomHorizontalAlignment = Alignment.End,
            stickyBottomContent = {
                BottomButton(
                    enabled = isPassphraseValid,
                    onCheckPassphrase = onCheckPassphrase,
                )
            },
        )

        else -> WalletLayouts.LargeContainerFloatingBottom(
            shouldScrollUnderTopBar = false,
            verticalArrangement = Arrangement.Top,
            content = {
                LargeContent(
                    enableBiometrics = enableBiometrics,
                    textFieldValue = textFieldValue,
                    passphraseInputFieldState = passphraseInputFieldState,
                    isPassphraseValid = isPassphraseValid,
                    initialSupportText = initialSupportText,
                    remainingAuthAttempts = remainingAuthAttempts,
                    isLoading = isLoading,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
        )
    }
    LoadingOverlay(isLoading)
}

@Composable
private fun CompactContent(
    enableBiometrics: Boolean,
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    initialSupportText: Boolean,
    remainingAuthAttempts: Int,
    isLoading: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    PassphraseInputComponent(
        modifier = Modifier.fillMaxWidth(),
        colors = WalletTextFieldColors.textFieldColors(),
        passphraseInputFieldState = passphraseInputFieldState,
        textFieldValue = textFieldValue,
        label = {
            Label(
                passphraseInputFieldState = passphraseInputFieldState,
            )
        },
        supportingText = {
            if (!isLoading) {
                SupportingText(
                    enableBiometrics = enableBiometrics,
                    initialSupportText = initialSupportText,
                    remainingAuthAttempts = remainingAuthAttempts,
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
    enableBiometrics: Boolean,
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    isPassphraseValid: Boolean,
    initialSupportText: Boolean,
    remainingAuthAttempts: Int,
    isLoading: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        PassphraseInputComponent(
            modifier = Modifier.weight(1f),
            colors = WalletTextFieldColors.textFieldColors(),
            passphraseInputFieldState = passphraseInputFieldState,
            textFieldValue = textFieldValue,
            label = {
                Label(
                    passphraseInputFieldState = passphraseInputFieldState,
                )
            },
            supportingText = {
                if (!isLoading) {
                    SupportingText(
                        enableBiometrics = enableBiometrics,
                        initialSupportText = initialSupportText,
                        remainingAuthAttempts = remainingAuthAttempts,
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
            enabled = isPassphraseValid,
            onCheckPassphrase = onCheckPassphrase
        )
    }
}

@Composable
private fun Label(
    passphraseInputFieldState: PassphraseInputFieldState,
) = WalletTexts.BodyLarge(
    text = stringResource(R.string.tk_changepassword_step1_note1),
    color = if (passphraseInputFieldState == PassphraseInputFieldState.Error) {
        WalletTheme.colorScheme.error
    } else {
        WalletTheme.colorScheme.onSurfaceVariant
    }
)

@Composable
private fun SupportingText(
    enableBiometrics: Boolean,
    initialSupportText: Boolean,
    remainingAuthAttempts: Int,
) = WalletTexts.BodySmall(
    text = if (initialSupportText) {
        if (enableBiometrics) {
            stringResource(R.string.tk_menu_activatingBiometrics_android_body)
        } else {
            stringResource(R.string.tk_menu_deactivatingBiometrics_android_note)
        }
    } else {
        pluralStringResource(R.plurals.tk_changepassword_error1_android_note2, remainingAuthAttempts, remainingAuthAttempts)
    },
    color = if (initialSupportText) WalletTheme.colorScheme.onSurfaceVariant else WalletTheme.colorScheme.error
)

@Composable
private fun BottomButton(
    enabled: Boolean,
    onCheckPassphrase: () -> Unit
) = Buttons.FilledPrimary(
    modifier = Modifier.testTag(TestTags.CONTINUE_BUTTON.name),
    text = stringResource(R.string.tk_global_continue),
    enabled = enabled,
    onClick = onCheckPassphrase
)

@WalletAllScreenPreview
@Composable
private fun AuthWithPassphraseScreenPreview() {
    WalletTheme {
        AuthWithPassphraseScreenContent(
            enableBiometrics = true,
            textFieldValue = TextFieldValue("abc123"),
            passphraseInputFieldState = PassphraseInputFieldState.Error,
            isPassphraseValid = true,
            initialSupportText = false,
            remainingAuthAttempts = 4,
            isLoading = false,
            onTextFieldValueChange = {},
            onCheckPassphrase = {},
        )
    }
}
