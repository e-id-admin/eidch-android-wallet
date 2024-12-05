package ch.admin.foitt.wallet.theme

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit

object WalletSpacers {

    @Composable
    fun VerticalTextSpacer(spacedBy: TextUnit) {
        with(LocalDensity.current) {
            Spacer(modifier = Modifier.height(spacedBy.toDp()))
        }
    }
}
