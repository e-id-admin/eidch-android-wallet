package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SubmittedDataBox(
    fields: List<String>,
    tintColor: Color = WalletTheme.colorScheme.onTertiaryContainer,
    backgroundColor: Color = WalletTheme.colorScheme.tertiaryContainer
) {
    Surface(
        shape = WalletTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .background(backgroundColor)
                .fillMaxWidth()
                .padding(horizontal = Sizes.s06, vertical = Sizes.s04)
        ) {
            FlowColumn(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                fields.map { fieldText ->
                    Field(fieldText, tintColor)
                }
            }
        }
    }
}

@Composable
private fun Field(
    text: String,
    tintColor: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.wallet_ic_check_circle_complete),
            tint = tintColor,
            contentDescription = null,
        )
        Spacer(Modifier.width(Sizes.s01))
        WalletTexts.Body(
            text = text,
            color = tintColor,
        )
        Spacer(Modifier.width(Sizes.s03))
    }
}

@Composable
@WalletComponentPreview
private fun MediumCredentialBoxPreview() {
    WalletTheme {
        SubmittedDataBox(
            fields = listOf("a", "b", "c"),
        )
    }
}
