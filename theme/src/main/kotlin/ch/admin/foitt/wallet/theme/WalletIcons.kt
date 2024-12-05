package ch.admin.foitt.wallet.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

object WalletIcons {

    @Composable
    fun IconWithBackground(
        icon: Painter,
        iconTint: Color = MaterialTheme.colorScheme.primary,
        backgroundColor: Color = PilotColors.grey08
    ) = Box(
        modifier = Modifier
            .size(Sizes.s08)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = iconTint,
        )
    }
}
