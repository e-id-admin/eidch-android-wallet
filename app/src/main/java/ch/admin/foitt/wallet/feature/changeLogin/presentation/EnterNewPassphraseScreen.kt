package ch.admin.foitt.wallet.feature.changeLogin.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.presentation.PassphraseInputComponent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun EnterNewPassphraseScreen(viewModel: EnterNewPassphraseViewModel) {
    EnterNewPassphraseScreenContent(
        passphrase = viewModel.passphrase.collectAsStateWithLifecycle().value,
        passphraseInputFieldState = viewModel.passphraseInputFieldState.collectAsStateWithLifecycle().value,
        isNextButtonEnabled = viewModel.isNextButtonEnabled.collectAsStateWithLifecycle().value,
        onUpdatePassphrase = viewModel::onUpdatePassphrase,
        onCheckPassphrase = viewModel::onCheckPassphrase,
    )
}

@Composable
private fun EnterNewPassphraseScreenContent(
    passphrase: String,
    passphraseInputFieldState: PassphraseInputFieldState,
    isNextButtonEnabled: Boolean,
    onUpdatePassphrase: (String) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    when (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> WalletLayouts.CompactContainerFloatingBottom(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            verticalArrangement = Arrangement.Top,
            content = {
                CompactContent(
                    passphrase = passphrase,
                    passphraseInputFieldState = passphraseInputFieldState,
                    onUpdatePassphrase = onUpdatePassphrase,
                    onCheckPassphrase = onCheckPassphrase
                )
            },
            stickyBottomHorizontalAlignment = Alignment.End,
            stickyBottomContent = {
                BottomButton(
                    enabled = isNextButtonEnabled,
                    onCheckPassphrase = onCheckPassphrase,
                )
            },
        )

        else -> WalletLayouts.LargeContainerFloatingBottom(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            useStatusBarPadding = false,
            verticalArrangement = Arrangement.Top,
            content = {
                LargeContent(
                    passphrase = passphrase,
                    passphraseInputFieldState = passphraseInputFieldState,
                    isNextButtonEnabled = isNextButtonEnabled,
                    onUpdatePassphrase = onUpdatePassphrase,
                    onCheckPassphrase = onCheckPassphrase
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
    Spacer(modifier = Modifier.height(Sizes.s04))
    PassphraseInputComponent(
        modifier = Modifier.fillMaxWidth(),
        colors = textFieldColors(),
        passphraseInputFieldState = passphraseInputFieldState,
        passphrase = passphrase,
        label = {
            Label(
                passphraseInputFieldState = passphraseInputFieldState,
            )
        },
        supportingText = {
            SupportingText()
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
    isNextButtonEnabled: Boolean,
    onUpdatePassphrase: (String) -> Unit,
    onCheckPassphrase: () -> Unit,
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        PassphraseInputComponent(
            modifier = Modifier.weight(1f),
            colors = textFieldColors(),
            passphraseInputFieldState = passphraseInputFieldState,
            passphrase = passphrase,
            label = {
                Label(
                    passphraseInputFieldState = passphraseInputFieldState,
                )
            },
            supportingText = {
                SupportingText()
            },
            keyboardImeAction = ImeAction.Next,
            onKeyboardAction = onCheckPassphrase,
            onPassphraseChange = onUpdatePassphrase,
            onAnimationFinished = {},
        )
        Spacer(modifier = Modifier.width(Sizes.s08))
        BottomButton(
            enabled = isNextButtonEnabled,
            onCheckPassphrase = onCheckPassphrase
        )
    }
}

@Composable
private fun textFieldColors() = TextFieldDefaults.colors().copy(
    focusedContainerColor = WalletTheme.colorScheme.background,
    unfocusedContainerColor = WalletTheme.colorScheme.background,
    errorContainerColor = WalletTheme.colorScheme.background,
    errorTextColor = WalletTheme.colorScheme.onSurfaceVariant,
    errorTrailingIconColor = WalletTheme.colorScheme.onSurfaceVariant,
)

@Composable
private fun Label(
    passphraseInputFieldState: PassphraseInputFieldState,
) = WalletTexts.BodyLarge(
    text = stringResource(R.string.tk_global_newpassword),
    color = if (passphraseInputFieldState == PassphraseInputFieldState.Error) {
        WalletTheme.colorScheme.error
    } else {
        WalletTheme.colorScheme.onSurfaceVariant
    }
)

@Composable
private fun SupportingText() = WalletTexts.BodySmall(
    text = stringResource(R.string.tk_changepassword_step2_note2),
    color = WalletTheme.colorScheme.onSurfaceVariant
)

@Composable
private fun BottomButton(
    enabled: Boolean,
    onCheckPassphrase: () -> Unit
) = Buttons.FilledPrimaryFixed(
    modifier = Modifier.testTag(TestTags.CONTINUE_BUTTON.name),
    text = stringResource(R.string.tk_global_continue),
    enabled = enabled,
    onClick = onCheckPassphrase
)

@WalletAllScreenPreview
@Composable
private fun EnterNewPassphraseScreenPreview() {
    WalletTheme {
        EnterNewPassphraseScreenContent(
            passphrase = "abc123",
            passphraseInputFieldState = PassphraseInputFieldState.Typing,
            isNextButtonEnabled = true,
            onUpdatePassphrase = {},
            onCheckPassphrase = {},
        )
    }
}
