package ch.admin.foitt.wallet.platform.biometrics.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletIcons
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun BiometricsContent(
    @StringRes header: Int,
    @StringRes description: Int,
    @StringRes infoText: Int,
) {
    WalletTexts.TitleScreenMultiLine(text = stringResource(id = header))
    Spacer(modifier = Modifier.height(Sizes.s04))
    BiometricsAvailableImage()
    Spacer(modifier = Modifier.height(Sizes.s04))
    WalletTexts.Body(text = stringResource(id = description))
    Spacer(modifier = Modifier.height(Sizes.s06))
    BiometricsInfoLabel(infoText = stringResource(id = infoText))
}

@Composable
internal fun OnboardingBiometricsContent(
    @StringRes title: Int,
    @StringRes description: Int,
    @StringRes infoText: Int?,
) {
    Spacer(modifier = Modifier.height(Sizes.s04))
    WalletTexts.TitleScreen(text = stringResource(id = title))
    Spacer(modifier = Modifier.height(Sizes.s04))
    WalletTexts.Body(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(id = description),
        color = WalletTheme.colorScheme.secondary,
    )
    infoText?.let {
        Spacer(modifier = Modifier.height(Sizes.s04))
        WalletTexts.LabelSmall(text = stringResource(id = infoText))
    }
}

@Composable
private fun BiometricsInfoLabel(
    infoText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        WalletIcons.IconWithBackground(
            icon = painterResource(id = R.drawable.pilot_ic_lock)
        )
        Spacer(modifier = Modifier.width(Sizes.s04))
        WalletTexts.LabelSmall(text = infoText)
    }
}
