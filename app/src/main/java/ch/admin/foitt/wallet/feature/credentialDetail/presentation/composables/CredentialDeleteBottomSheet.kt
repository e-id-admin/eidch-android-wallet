package ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletButtonColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialDeleteBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onDeleteCredential: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = WalletTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.semantics {
            isTraversalGroup = true
            traversalIndex = -5f
        },
        dragHandle = {}
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Sizes.s06, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(Sizes.s04)
                .verticalScroll(rememberScrollState())

        ) {
            Spacer(modifier = Modifier.height(Sizes.s04))
            Text(
                stringResource(R.string.tk_displaydelete_credentialdelete_title),
                style = WalletTheme.typography.headlineSmall
            )
            WalletTexts.BodyLarge(
                stringResource(R.string.tk_displaydelete_credentialdelete_body),
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.padding(Sizes.s06),
                horizontalArrangement = Arrangement.spacedBy(Sizes.s02)
            ) {
                Buttons.FilledSecondary(
                    text = stringResource(R.string.tk_global_cancel),
                    onClick = { onDismiss() }
                )
                Buttons.Text(
                    text = stringResource(R.string.tk_global_delete),
                    onClick = { onDeleteCredential() },
                    colors = WalletButtonColors.text().copy(contentColor = WalletTheme.colorScheme.onLightError)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@WalletAllScreenPreview
@Composable
private fun CredentialDeleteBottomSheetPreview() {
    WalletTheme {
        CredentialDeleteBottomSheet(
            sheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Expanded
            ),
            onDeleteCredential = {},
            onDismiss = {}
        )
    }
}
