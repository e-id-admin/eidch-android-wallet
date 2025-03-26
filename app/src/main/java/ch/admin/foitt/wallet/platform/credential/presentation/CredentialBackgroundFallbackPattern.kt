package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.admin.foitt.wallet.R

@Composable
fun CredentialBackgroundFallbackPattern(
    modifier: Modifier = Modifier,
    useSmall: Boolean = false,
) {
    BackgroundCanvas(
        modifier = modifier,
        drawableId = if (useSmall) {
            R.drawable.wallet_ic_credential_background_fallback_pattern_small
        } else {
            R.drawable.wallet_ic_credential_background_fallback_pattern
        },
    )
}
