package ch.admin.foitt.wallet.platform.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun PassphraseValidationErrorToastFixed(
    modifier: Modifier = Modifier,
    @StringRes text: Int = R.string.tk_global_warning_alt,
    iconEndContentDescription: Int? = R.string.tk_global_closewarning_alt,
    onIconEnd: () -> Unit,
) = Toast(
    modifier = modifier,
    backgroundColor = WalletTheme.colorScheme.lightErrorFixed,
    iconStart = R.drawable.wallet_ic_warning,
    iconStartColor = WalletTheme.colorScheme.onLightErrorFixed,
    text = text,
    textColor = WalletTheme.colorScheme.onLightErrorFixed,
    iconEnd = R.drawable.wallet_ic_cross,
    iconEndColor = WalletTheme.colorScheme.onLightErrorFixed,
    iconEndContentDescription = iconEndContentDescription,
    onIconEnd = onIconEnd
)

@Composable
fun PassphraseValidationErrorToast(
    modifier: Modifier = Modifier,
    @StringRes text: Int = R.string.tk_global_warning_alt,
    iconEndContentDescription: Int? = R.string.tk_global_closewarning_alt,
    onIconEnd: () -> Unit,
) = Toast(
    modifier = modifier,
    backgroundColor = WalletTheme.colorScheme.lightError,
    iconStart = R.drawable.wallet_ic_warning,
    iconStartColor = WalletTheme.colorScheme.onLightError,
    text = text,
    textColor = WalletTheme.colorScheme.onLightError,
    iconEnd = R.drawable.wallet_ic_cross,
    iconEndColor = WalletTheme.colorScheme.onLightError,
    iconEndContentDescription = iconEndContentDescription,
    onIconEnd = onIconEnd
)

@Composable
fun Toast(
    modifier: Modifier = Modifier,
    backgroundColor: Color = WalletTheme.colorScheme.surface,
    @StringRes headline: Int? = null,
    headlineColor: Color = WalletTheme.colorScheme.onSurface,
    @StringRes text: Int,
    textColor: Color = WalletTheme.colorScheme.onSurfaceVariant,
    @StringRes linkText: Int? = null,
    @DrawableRes iconStart: Int? = null,
    iconStartColor: Color = WalletTheme.colorScheme.onSurfaceVariant,
    @DrawableRes iconEnd: Int? = R.drawable.wallet_ic_cross,
    iconEndColor: Color = WalletTheme.colorScheme.onSurfaceVariant,
    @StringRes iconEndContentDescription: Int? = null,
    onLink: () -> Unit = {},
    onIconEnd: () -> Unit = {},
) = Surface(
    modifier = modifier,
    shadowElevation = Sizes.line02,
    shape = RoundedCornerShape(Sizes.s04),
    color = backgroundColor,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Sizes.s04, top = Sizes.s03, bottom = Sizes.s03, end = Sizes.s01)
            .testTag(TestTags.ERROR.name),
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconStart?.let {
            Icon(
                painter = painterResource(id = iconStart),
                contentDescription = null,
                tint = iconStartColor,
            )
            Spacer(modifier = Modifier.width(Sizes.s04))
        }
        Column(
            modifier = Modifier.weight(1f)
        ) {
            headline?.let {
                WalletTexts.TitleSmall(
                    text = stringResource(id = headline),
                    color = headlineColor,
                    modifier = Modifier.semantics {
                        traversalIndex = -5f
                    },
                )
            }

            WalletTexts.LabelLarge(
                text = stringResource(id = text),
                color = textColor,
                modifier = Modifier.semantics {
                    traversalIndex = -4f
                },
            )
            linkText?.let {
                Spacer(modifier = Modifier.height(Sizes.s01))
                Buttons.TextLink(
                    text = stringResource(id = linkText),
                    endIcon = painterResource(id = R.drawable.wallet_ic_chevron),
                    onClick = onLink,
                    modifier = Modifier.semantics {
                        traversalIndex = -3f
                    },
                )
            }
        }
        iconEnd?.let {
            Spacer(modifier = Modifier.width(Sizes.s02))
            IconButton(
                onClick = onIconEnd,
                modifier = Modifier.spaceBarKeyClickable(onIconEnd),
            ) {
                Icon(
                    modifier = Modifier
                        .clip(CircleShape)
                        .semantics {
                            traversalIndex = 1f
                        },
                    painter = painterResource(id = iconEnd),
                    contentDescription = iconEndContentDescription?.let {
                        stringResource(iconEndContentDescription)
                    },
                    tint = iconEndColor,
                )
            }
        }
    }
}

@WalletComponentPreview
@Composable
private fun ToastPreview() {
    WalletTheme {
        Toast(
            headline = R.string.tk_global_warning_alt,
            text = R.string.tk_onboarding_start_body,
            linkText = R.string.tk_global_warning_alt,
            iconStart = R.drawable.wallet_ic_qr,
            iconEnd = R.drawable.wallet_ic_cross,
            onLink = { },
            onIconEnd = { },
        )
    }
}

@WalletComponentPreview
@Composable
private fun ErrorToastPreview() {
    WalletTheme {
        PassphraseValidationErrorToast(
            text = R.string.tk_global_warning_alt,
            onIconEnd = {},
        )
    }
}
