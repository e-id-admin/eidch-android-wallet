package ch.admin.foitt.wallet.platform.invitation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.ErrorScreenContent
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationErrorScreenState
import ch.admin.foitt.wallet.platform.navArgs.domain.model.InvitationFailureNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@Destination(
    navArgsDelegate = InvitationFailureNavArg::class,
)
fun InvitationFailureScreen(
    viewModel: InvitationFailureViewModel,
) {
    InvitationFailureScreenContent(
        screenState = viewModel.error,
        onClose = viewModel::close,
    )
}

@Composable
private fun InvitationFailureScreenContent(
    screenState: InvitationErrorScreenState,
    onClose: () -> Unit,
) = when (screenState) {
    InvitationErrorScreenState.INVALID_CREDENTIAL -> InvalidCredential(onClose)
    InvitationErrorScreenState.UNKNOWN_ISSUER -> UnknownIssuer(onClose)
    InvitationErrorScreenState.NETWORK_ERROR -> NetworkError(onClose)
    InvitationErrorScreenState.UNEXPECTED -> UnexpectedError(onClose)
}

@Composable
private fun InvalidCredential(onClose: () -> Unit) = ErrorScreenContent(
    iconRes = R.drawable.wallet_ic_error_credential,
    title = stringResource(id = R.string.tk_error_invitationcredential_title),
    body = stringResource(id = R.string.tk_error_invitationcredential_body),
    primaryButton = stringResource(id = R.string.tk_global_close),
    onPrimaryClick = onClose,
)

@Composable
private fun UnknownIssuer(onClose: () -> Unit) = ErrorScreenContent(
    iconRes = R.drawable.wallet_ic_error_questionmark,
    title = stringResource(id = R.string.tk_error_issuer_notregistered_title),
    body = stringResource(id = R.string.tk_error_issuer_notregistered_body),
    primaryButton = stringResource(id = R.string.tk_global_close),
    onPrimaryClick = onClose,
)

@Composable
private fun NetworkError(onClose: () -> Unit) = ErrorScreenContent(
    iconRes = R.drawable.wallet_ic_error_network,
    title = stringResource(id = R.string.tk_error_connectionproblem_title),
    body = stringResource(id = R.string.tk_error_connectionproblem_body),
    primaryButton = stringResource(id = R.string.tk_global_close),
    onPrimaryClick = onClose,
)

@Composable
private fun UnexpectedError(onClose: () -> Unit) = ErrorScreenContent(
    iconRes = R.drawable.wallet_ic_error_general,
    title = stringResource(id = R.string.global_error_unexpected_title),
    body = stringResource(id = R.string.global_error_unexpected_message),
    primaryButton = stringResource(id = R.string.tk_global_close),
    onPrimaryClick = onClose,
)

private class InvitationFailureParams : PreviewParameterProvider<InvitationErrorScreenState> {
    override val values: Sequence<InvitationErrorScreenState> = sequenceOf(
        InvitationErrorScreenState.INVALID_CREDENTIAL,
        InvitationErrorScreenState.NETWORK_ERROR,
        InvitationErrorScreenState.UNKNOWN_ISSUER,
        InvitationErrorScreenState.UNEXPECTED,
    )
}

@WalletAllScreenPreview
@Composable
private fun InvitationFailureScreenPreview(
    @PreviewParameter(InvitationFailureParams::class) screenState: InvitationErrorScreenState,
) {
    WalletTheme {
        InvitationFailureScreenContent(
            screenState = screenState,
            onClose = {},
        )
    }
}
