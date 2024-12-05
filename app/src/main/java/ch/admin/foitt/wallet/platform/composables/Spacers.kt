package ch.admin.foitt.wallet.platform.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun SpacerTop(
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    useStatusBarInsets: Boolean = false,
) = Column(
    modifier = modifier.zIndex(1.0f)
) {
    val usedBackgroundColor = if (useStatusBarInsets) {
        backgroundColor.copy(alpha = 0.75f)
    } else {
        backgroundColor
    }

    if (useStatusBarInsets) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = usedBackgroundColor)
                .statusBarsPadding()
        )
    }
}

@Composable
fun SpacerBottom(
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    useNavigationBarInsets: Boolean = false,
) = Column(
    modifier = modifier,
) {
    val usedBackgroundColor = if (useNavigationBarInsets) {
        backgroundColor.copy(alpha = 0.75f)
    } else {
        backgroundColor
    }
    if (useNavigationBarInsets) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = usedBackgroundColor)
                .navigationBarsPadding()
        )
    }
}

@WalletComponentPreview
@Composable
private fun TopSpacerPreview() {
    WalletTheme {
        Column(
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            SpacerTop(backgroundColor = Color.Magenta)
            SpacerBottom(backgroundColor = Color.Blue)
        }
    }
}
