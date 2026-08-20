package ch.admin.foitt.wallet.platform.activityList.presentation.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun DestructiveActionButton(
    @StringRes title: Int,
    @DrawableRes leadingIcon: Int,
    onClick: () -> Unit,
) = ListItem(
    modifier = Modifier
        .clickable(onClick = onClick)
        .semantics {
            role = Role.Button
        }
        .spaceBarKeyClickable(onSpace = onClick),
    colors = ListItemDefaults.colors(containerColor = WalletTheme.colorScheme.listItemBackground),
    headlineContent = {
        WalletTexts.BodyLarge(
            text = stringResource(title),
            color = WalletTheme.colorScheme.onLightError,
        )
    },
    leadingContent = {
        Icon(
            painter = painterResource(leadingIcon),
            contentDescription = null,
            tint = WalletTheme.colorScheme.onLightError,
        )
    }
)
