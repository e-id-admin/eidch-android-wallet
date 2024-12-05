package ch.admin.foitt.wallet.platform.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Gradients
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun BiometricsScreenImage(
    showSubtitle: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(Sizes.loginMethodHeaderMaxHeight)
            .background(Gradients.biometricsOnboardingBrush()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.pilot_ic_biometrics_onboarding),
            contentDescription = null
        )
        if (showSubtitle) {
            Spacer(modifier = Modifier.height(Sizes.s02))
            WalletTexts.Body(text = stringResource(id = R.string.login_biometrics_subtitle))
        }
    }
}

private class BiometricsScreenImagePreviewParamsProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(true, false)
}

@WalletComponentPreview
@Composable
fun BiometricsScreenImagePreview(
    @PreviewParameter(BiometricsScreenImagePreviewParamsProvider::class) showSubtitle: Boolean,
) {
    WalletTheme {
        BiometricsScreenImage(showSubtitle)
    }
}
