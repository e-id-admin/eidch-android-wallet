package ch.admin.foitt.wallet.platform.eIdApplicationProcess.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.presentation.model.MrzData
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.setIsTraversalGroup
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun MrzChooserScreen(
    viewModel: MrzChooserViewModel
) {
    MrzChooserScreenContent(
        screenData = viewModel.mrzData,
        onMrzItemClick = viewModel::onMrzItemClick,
    )
}

@Composable
private fun MrzChooserScreenContent(
    screenData: List<MrzData>,
    onMrzItemClick: (Int) -> Unit,
) {
    WalletLayouts.LazyColumn(
        useBottomInsets = false,
        modifier = Modifier
            .setIsTraversalGroup()
            .fillMaxHeight(),
        contentPadding = PaddingValues(
            top = Sizes.s06,
            bottom = Sizes.s06
        )
    ) {
        itemsIndexed(screenData) { index, state ->
            WalletListItems.SimpleListItem(
                leadingIcon = R.drawable.wallet_ic_account,
                title = "${index + 1} ${state.displayName}",
                onItemClick = { onMrzItemClick(index) },
                trailingIcon = R.drawable.pilot_ic_settings_next,
            )
        }
    }
}

@WalletAllScreenPreview
@Composable
fun MrzScreenPreview() {
    WalletTheme {
        MrzChooserScreenContent(
            screenData = listOf(
                MrzData(
                    displayName = "1 Adult (ID-CARD)",
                    payload = ApplyRequest(
                        mrz = listOf(),
                    )
                ),
                MrzData(
                    displayName = "2 Adult (PASSPORT)",
                    payload = ApplyRequest(
                        mrz = listOf(),
                    )
                ),
                MrzData(
                    displayName = "1 Underage (ID-CARD)",
                    payload = ApplyRequest(
                        mrz = listOf(),
                    )
                )
            ),
            onMrzItemClick = {},
        )
    }
}
