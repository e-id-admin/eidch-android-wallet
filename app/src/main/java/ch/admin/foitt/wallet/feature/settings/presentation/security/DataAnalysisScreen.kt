package ch.admin.foitt.wallet.feature.settings.presentation.security

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun DataAnalysisScreen() {
    DataAnalysisScreenContent()
}

@Composable
private fun DataAnalysisScreenContent() {
    Column(
        modifier = Modifier
            .addTopScaffoldPadding()
            .verticalScroll(rememberScrollState())
            .horizontalSafeDrawing()
            .bottomSafeDrawing()
            .padding(
                start = Sizes.s08,
                top = Sizes.s05,
                end = Sizes.s08,
                bottom = Sizes.s05
            ),
    ) {
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.dataAnalysis_title),
        )
        Spacer(modifier = Modifier.height(Sizes.s10))
        WalletTexts.Body(
            text = stringResource(id = R.string.dataAnalysis_text),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun DataAnalysisScreenContentPreview() {
    WalletTheme {
        DataAnalysisScreenContent()
    }
}
