package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SubmittedDataBox(
    fields: List<String>
) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        shadowElevation = Sizes.line01,
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .padding(horizontal = Sizes.s04, vertical = Sizes.s04)
        ) {
            WalletTexts.LabelMedium(
                text = stringResource(id = R.string.presentation_result_list_title),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.size(Sizes.s02))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                fields.map { fieldText ->
                    Field(fieldText)
                }
            }
        }
    }
}

@Composable
private fun Field(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.pilot_ic_checkmark),
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = null,
        )
        Spacer(Modifier.width(Sizes.s01))
        WalletTexts.BodySmall(text = text)
        Spacer(Modifier.width(Sizes.s02))
    }
}
