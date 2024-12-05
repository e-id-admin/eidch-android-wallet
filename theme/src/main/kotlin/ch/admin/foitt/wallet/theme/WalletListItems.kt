package ch.admin.foitt.wallet.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString

object WalletListItems {

    @Composable
    fun SimpleListItem(
        @DrawableRes leadingIcon: Int? = null,
        title: String,
        onItemClick: () -> Unit,
        @DrawableRes trailingIcon: Int,
        showDivider: Boolean = true,
    ) {
        ListItem(
            modifier = Modifier.clickable {
                onItemClick()
            },
            leadingContent = leadingIcon?.let {
                {
                    ListItemIcon(icon = leadingIcon)
                }
            },
            headlineContent = {
                WalletTexts.Body(
                    text = title,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = { ListItemIcon(icon = trailingIcon) },
        )
        if (showDivider) {
            val startPadding = if (leadingIcon != null) Sizes.s15 else Sizes.s04
            HorizontalDivider(modifier = Modifier.padding(start = startPadding, end = Sizes.s06))
        }
    }

    @Composable
    fun SwitchListItem(
        title: String,
        description: AnnotatedString? = null,
        isSwitchEnabled: Boolean = true,
        isSwitchChecked: Boolean,
        onSwitchChange: (Boolean) -> Unit,
        showDivider: Boolean = true,
    ) {
        Column(
            modifier = Modifier
                .defaultMinSize(Sizes.s14)
                .padding(start = Sizes.s04, end = Sizes.s04),
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.height(Sizes.s02))
            Row(verticalAlignment = Alignment.CenterVertically) {
                WalletTexts.Body(
                    modifier = Modifier.weight(1f),
                    text = title,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(Sizes.s04))
                Switch(
                    enabled = isSwitchEnabled,
                    checked = isSwitchChecked,
                    onCheckedChange = onSwitchChange,
                )
            }
            description?.let {
                WalletTexts.LabelMedium(
                    modifier = Modifier.fillMaxWidth(),
                    text = description,
                )
                Spacer(modifier = Modifier.height(Sizes.s04))
            }
        }
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(start = Sizes.s04, end = Sizes.s06))
        }
    }

    @Composable
    private fun ListItemIcon(icon: Int) {
        Icon(
            painterResource(id = icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
