package ch.admin.foitt.wallet.platform.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun CredentialDetailBottomSheetItem(
    @DrawableRes icon: Int,
    title: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .spaceBarKeyClickable(onClick)
            .height(Sizes.s16)
            .padding(start = Sizes.s04),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier
                .width(Sizes.s06)
                .align(Alignment.CenterVertically),
            tint = color
        )
        Spacer(modifier = Modifier.width(Sizes.s03))
        Text(text = title, modifier = Modifier.align(Alignment.CenterVertically), color = color)
    }
}

private fun interface ComposableColor {
    @Composable
    fun value(): Color
}

private class ColorProvider : PreviewParameterProvider<ComposableColor> {
    override val values: Sequence<ComposableColor> = sequenceOf(
        object : ComposableColor {
            @Composable
            override fun value(): Color = MaterialTheme.colorScheme.onSurface
        },
        object : ComposableColor {
            @Composable
            override fun value(): Color = MaterialTheme.colorScheme.error
        },
    )
}

@WalletComponentPreview
@Composable
private fun CredentialDetailBottomSheetItemPreview(
    @PreviewParameter(ColorProvider::class) color: ComposableColor
) {
    WalletTheme {
        Column {
            CredentialDetailBottomSheetItem(
                icon = R.drawable.wallet_ic_trashcan,
                title = "Some title",
                onClick = {},
                color = color.value()
            )
            CredentialDetailBottomSheetItem(
                icon = R.drawable.ic_swiss_cross_small,
                title = "Some title",
                onClick = {},
                color = color.value()
            )
        }
    }
}
