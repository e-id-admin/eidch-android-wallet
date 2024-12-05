package ch.admin.foitt.wallet.feature.qrscan.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Toast

@Composable
fun QrToastHint(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.qrScanner_toast_hint_title,
    text = R.string.qrScanner_toast_hint_message,
    linkText = null,
    iconStart = R.drawable.wallet_ic_qr,
    onLink = { },
    onClose = onClose,
)

@Composable
fun QrToastInvalidQr(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onLink: (linkRes: Int) -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.qrScanner_toast_invalidQr_title,
    text = R.string.qrScanner_toast_invalidQr_message,
    linkText = R.string.qrScanner_toast_invalidQr_link_text,
    iconStart = R.drawable.wallet_ic_qr,
    onLink = { onLink(R.string.qrScanner_toast_invalidQr_link) },
    onClose = onClose,
)

@Composable
fun QrToastEmptyWallet(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.qrScanner_toast_emptyWallet_title,
    text = R.string.qrScanner_toast_emptyWallet_message,
    linkText = null,
    iconStart = R.drawable.wallet_ic_questionmark,
    onLink = { },
    onClose = onClose,
)

@Composable
fun QrToastNoCompatibleCredential(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onLink: (linkRes: Int) -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.qrScanner_toast_noCompatibleCredential_title,
    text = R.string.qrScanner_toast_noCompatibleCredential_message,
    linkText = R.string.qrScanner_toast_noCompatibleCredential_link_text,
    iconStart = R.drawable.wallet_ic_account,
    onLink = { onLink(R.string.qrScanner_toast_noCompatibleCredential_link) },
    onClose = onClose,
)

@Composable
fun QrToastNetworkError(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.qrScanner_toast_networkError_title,
    text = R.string.qrScanner_toast_networkError_message,
    linkText = null,
    iconStart = R.drawable.wallet_ic_wifi,
    onLink = { },
    onClose = onClose,
)

@Composable
fun QrToastUnexpectedError(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.qrScanner_toast_unexpectedError_title,
    text = R.string.qrScanner_toast_unexpectedError_message,
    linkText = null,
    iconStart = R.drawable.wallet_ic_questionmark,
    onLink = { },
    onClose = onClose,
)

@Composable
fun QrToastInvalidPresentation(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) = QrInfoToast(
    modifier = modifier,
    headline = R.string.qrScanner_toast_invalidPresentation_title,
    text = R.string.qrScanner_toast_invalidPresentation_message,
    linkText = null,
    iconStart = R.drawable.wallet_ic_questionmark,
    onLink = { },
    onClose = onClose,
)

@Composable
private fun QrInfoToast(
    modifier: Modifier = Modifier,
    @StringRes headline: Int,
    @StringRes text: Int,
    @StringRes linkText: Int?,
    @DrawableRes iconStart: Int,
    onLink: () -> Unit,
    onClose: () -> Unit,
) = Toast(
    modifier = modifier,
    headline = headline,
    text = text,
    linkText = linkText,
    iconEndContentDescription = R.string.qrScanner_toast_close_button,
    iconStart = iconStart,
    iconEnd = R.drawable.wallet_ic_cross,
    onLink = onLink,
    onIconEnd = onClose,
)
