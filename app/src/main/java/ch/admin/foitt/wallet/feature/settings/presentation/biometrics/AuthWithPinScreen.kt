package ch.admin.foitt.wallet.feature.settings.presentation.biometrics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.PassphraseValidationErrorToast
import ch.admin.foitt.wallet.platform.navArgs.domain.model.AuthWithPinNavArg
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.presentation.PassphraseInputComponent
import ch.admin.foitt.wallet.theme.WalletTexts
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    navArgsDelegate = AuthWithPinNavArg::class,
)
@Composable
fun AuthWithPinScreen(
    viewModel: AuthWithPinViewModel
) {
    AuthWithPinScreenContent(
        enableBiometrics = viewModel.enableBiometrics,
        passphraseInputFieldState = viewModel.passphraseInputFieldState.collectAsStateWithLifecycle().value,
        passphrase = viewModel.passphrase.collectAsStateWithLifecycle().value,
        errorMessage = viewModel.errorMessage.collectAsStateWithLifecycle().value,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        onUpdatePassphrase = viewModel::onUpdatePassphrase,
        onCheckPassphrase = viewModel::onCheckPassphrase,
        onClosePassphraseError = viewModel::onClosePassphraseError,
    )
}

@Composable
private fun AuthWithPinScreenContent(
    enableBiometrics: Boolean,
    passphraseInputFieldState: PassphraseInputFieldState,
    passphrase: String,
    errorMessage: Int?,
    isLoading: Boolean,
    onUpdatePassphrase: (String) -> Unit,
    onCheckPassphrase: () -> Unit,
    onClosePassphraseError: () -> Unit,
) = Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.align(Alignment.TopCenter)) {
        val description = if (enableBiometrics) {
            R.string.change_biometrics_pin_activation_content_text
        } else {
            R.string.change_biometrics_pin_deactivation_content_text
        }
        WalletTexts.Body(text = stringResource(description))
        PassphraseInputComponent(
            modifier = Modifier.fillMaxWidth(),
            passphraseInputFieldState = passphraseInputFieldState,
            passphrase = passphrase,
            enabled = !isLoading,
            keyboardImeAction = ImeAction.Go,
            onKeyboardAction = onCheckPassphrase,
            onPassphraseChange = onUpdatePassphrase,
            onAnimationFinished = {},
        )
        errorMessage?.let {
            PassphraseValidationErrorToast(
                modifier = Modifier.fillMaxWidth(),
                text = errorMessage,
                onIconEnd = onClosePassphraseError,
            )
        }
    }
    Buttons.FilledPrimary(
        modifier = Modifier.align(Alignment.BottomEnd),
        text = "weiter",
        onClick = onCheckPassphrase,
    )
    LoadingOverlay(isLoading)
}
