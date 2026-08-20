package ch.admin.foitt.wallet.feature.changeLogin.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.WindowWidthClass
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.windowWidthClass
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.presentation.PassphraseInputComponent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTextFieldColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EnterCurrentPassphraseScreen(viewModel: EnterCurrentPassphraseViewModel) {
    OnResumeEventHandler {
        viewModel.checkRemainingAttempts()
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value

    LaunchedEffect(isLoading) {
        when (isLoading) {
            true -> keyboardController?.hide()
            false -> keyboardController?.show()
        }
    }

    EnterCurrentPassphraseScreenContent(
        passphraseInputFieldState = viewModel.passphraseInputFieldState.collectAsStateWithLifecycle().value,
        textFieldValue = viewModel.textFieldValue.collectAsStateWithLifecycle().value,
        isPassphraseValid = viewModel.isPassphraseValid.collectAsStateWithLifecycle().value,
        hideSupportText = viewModel.hideSupportText.collectAsStateWithLifecycle().value,
        attemptsLeft = viewModel.remainingAuthAttempts.collectAsStateWithLifecycle().value,
        isLoading = isLoading,
        onTextFieldValueChange = viewModel::onTextFieldValueChange,
        onCheckPassphrase = viewModel::onCheckPassphrase,
    )
}

@Composable
private fun EnterCurrentPassphraseScreenContent(
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    isPassphraseValid: Boolean,
    hideSupportText: Boolean,
    attemptsLeft: Int,
    isLoading: Boolean,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    when (currentWindowAdaptiveInfoV2().windowWidthClass()) {
        WindowWidthClass.COMPACT -> WalletLayouts.CompactContainerFloatingBottom(
            modifier = Modifier.background(WalletTheme.colorScheme.surfaceContainerLow),
            shouldScrollUnderTopBar = false,
            verticalArrangement = Arrangement.Top,
            content = {
                CompactContent(
                    textFieldValue = textFieldValue,
                    passphraseInputFieldState = passphraseInputFieldState,
                    hideSupportText = hideSupportText,
                    attemptsLeft = attemptsLeft,
                    onTextFieldValueChange = onTextFieldValueChange,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
            stickyBottomContent = {
                AdaptiveBottomButtonBar(
                    buttons = listOf(
                        {
                            BottomButton(
                                enabled = isPassphraseValid,
                                onCheckPassphrase = onCheckPassphrase,
                            )
                        }
                    )
                )
            },
        )

        else -> WalletLayouts.LargeContainerFloatingBottom(
            modifier = Modifier.background(WalletTheme.colorScheme.surfaceContainerLow),
            shouldScrollUnderTopBar = false,
            verticalArrangement = Arrangement.Top,
            content = {
                LargeContent(
                    textFieldValue = textFieldValue,
                    passphraseInputFieldState = passphraseInputFieldState,
                    isPassphraseValid = isPassphraseValid,
                    hideSupportText = hideSupportText,
                    attemptsLeft = attemptsLeft,
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
    textFieldValue: TextFieldValue,
    passphraseInputFieldState: PassphraseInputFieldState,
    hideSupportText: Boolean,
    attemptsLeft: Int,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    PassphraseInputComponent(
        modifier = Modifier.fillMaxWidth(),
        colors = WalletTextFieldColors.textFieldColors(),
        passphraseInputFieldState = passphraseInputFieldState,
        errorMessage = errorMessage(
            hideSupportText = hideSupportText,
            attemptsLeft = attemptsLeft,
        ),
        textFieldValue = textFieldValue,
        label = {
            Label(
                passphraseInputFieldState = passphraseInputFieldState,
            )
        },
        supportingText = {
            if (!hideSupportText) {
                SupportingText(
                    attemptsLeft = attemptsLeft,
                    isError = passphraseInputFieldState is PassphraseInputFieldState.Error,
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
    isPassphraseValid: Boolean,
    hideSupportText: Boolean,
    attemptsLeft: Int,
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
            errorMessage = errorMessage(
                hideSupportText = hideSupportText,
                attemptsLeft = attemptsLeft,
            ),
            textFieldValue = textFieldValue,
            label = {
                Label(
                    passphraseInputFieldState = passphraseInputFieldState,
                )
            },
            supportingText = {
                if (!hideSupportText) {
                    SupportingText(
                        attemptsLeft = attemptsLeft,
                        isError = passphraseInputFieldState is PassphraseInputFieldState.Error,
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
private fun attemptsLeftText(
    attemptsLeft: Int,
): String = pluralStringResource(
    R.plurals.tk_changepassword_error1_android_note2,
    attemptsLeft,
    attemptsLeft
)

@Composable
private fun errorMessage(
    hideSupportText: Boolean,
    attemptsLeft: Int,
): String? = if (hideSupportText) {
    null
} else {
    attemptsLeftText(attemptsLeft)
}

@Composable
private fun SupportingText(
    attemptsLeft: Int,
    isError: Boolean,
) = WalletTexts.BodySmall(
    // In the error state the attempts-left info is already announced via the field's
    // error() semantics (see errorMessage), so clear semantics here to avoid a double
    // announcement. When not in error (e.g. arriving with attempts already reduced), the
    // error() semantics is absent, so keep this readable as the only carrier of the info.
    modifier = if (isError) Modifier.clearAndSetSemantics {} else Modifier,
    text = attemptsLeftText(attemptsLeft),
    color = WalletTheme.colorScheme.error
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
private fun EnterCurrentPassphraseScreenPreview() {
    WalletTheme {
        EnterCurrentPassphraseScreenContent(
            textFieldValue = TextFieldValue("abc123"),
            passphraseInputFieldState = PassphraseInputFieldState.Error,
            isPassphraseValid = true,
            hideSupportText = false,
            attemptsLeft = 4,
            isLoading = false,
            onTextFieldValueChange = {},
            onCheckPassphrase = {},
        )
    }
}
