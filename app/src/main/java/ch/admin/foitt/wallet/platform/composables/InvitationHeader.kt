package ch.admin.foitt.wallet.platform.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletSpacers.VerticalTextSpacer
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun InvitationHeader(
    inviterName: String,
    inviterImage: Painter?,
    message: String,
    modifier: Modifier = Modifier,
) = Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top,
) {
    Avatar(inviterImage, size = AvatarSize.LARGE)
    Spacer(modifier = Modifier.size(Sizes.s04))
    Column(
        modifier = Modifier
            .weight(1f)
            .semantics(mergeDescendants = true) {}
    ) {
        WalletTexts.TitleMedium(
            text = inviterName,
            color = WalletTheme.colorScheme.onSurface
        )
        VerticalTextSpacer(spacedBy = 8.sp)
        WalletTexts.TitleMedium(
            text = message,
            color = WalletTheme.colorScheme.onSurfaceVariant
        )
    }
}
