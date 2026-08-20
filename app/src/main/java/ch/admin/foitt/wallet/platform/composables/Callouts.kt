package ch.admin.foitt.wallet.platform.composables

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

object Callouts {
    @Composable
    fun ReportedActor(
        modifier: Modifier = Modifier,
        @StringRes label: Int,
        endIcon: Painter? = painterResource(R.drawable.wallet_ic_info),
        onClick: (() -> Unit)? = null
    ) = BaseCallout(
        modifier = modifier,
        label = label,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        onClick = onClick,
        trailingContent = {
            endIcon?.let {
                Icon(
                    modifier = Modifier.size(Sizes.s06),
                    painter = endIcon,
                    contentDescription = null,
                    tint = WalletTheme.colorScheme.onLightOrange,
                )
            }
        }
    )

    @Composable
    fun UnregisteredRequest(
        modifier: Modifier = Modifier,
        @StringRes label: Int,
        leadingIcon: Painter? = painterResource(R.drawable.wallet_ic_shield_alert),
        onClick: (() -> Unit)? = null
    ) = BaseCallout(
        modifier = modifier,
        label = label,
        textAlign = TextAlign.Start,
        onClick = onClick,
        leadingContent = {
            leadingIcon?.let {
                Icon(
                    modifier = Modifier.size(Sizes.s06),
                    painter = leadingIcon,
                    contentDescription = null,
                    tint = WalletTheme.colorScheme.onLightOrange,
                )
            }
        }
    )
}

@Composable
private fun BaseCallout(
    modifier: Modifier = Modifier,
    @StringRes label: Int,
    textAlign: TextAlign = TextAlign.Center,
    tint: Color = WalletTheme.colorScheme.onLightOrange,
    background: Color = WalletTheme.colorScheme.calloutSurface,
    borderColor: Color = WalletTheme.colorScheme.calloutBorder,
    borderWidth: Dp = Sizes.line01,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    onClick: (() -> Unit)?,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Sizes.s02))
            .background(background)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(Sizes.s02)
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick).spaceBarKeyClickable(onSpace = onClick) else Modifier
            )
            .semantics { role = Role.Button }
            .padding(horizontal = Sizes.s04, vertical = Sizes.s03),
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
    ) {
        leadingContent?.let {
            leadingContent()
            Spacer(modifier = Modifier.size(Sizes.s02))
        }
        WalletTexts.BodyMedium(
            modifier = Modifier.weight(1f),
            text = stringResource(label),
            textAlign = textAlign,
            color = tint,
        )

        trailingContent?.let {
            Spacer(modifier = Modifier.size(Sizes.s02))
            trailingContent()
        }
    }
}

@WalletComponentPreview
@Composable
private fun CalloutsPreview() {
    WalletTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Sizes.s03)) {
            Callouts.ReportedActor(
                label = R.string.tk_actor_nonCompliant_button,
                onClick = {}
            )
            Callouts.UnregisteredRequest(
                label = R.string.tk_present_unregisteredRequest_warning,
                onClick = {}
            )
        }
    }
}
