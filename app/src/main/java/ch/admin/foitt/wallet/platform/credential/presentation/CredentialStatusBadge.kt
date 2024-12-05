package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun CredentialStatusBadge(
    status: CredentialStatus?,
    textColor: Color,
    modifier: Modifier = Modifier,
) = AnimatedContent(
    modifier = modifier,
    targetState = status,
    transitionSpec = {
        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
    },
    label = "fadingAnimation",
) { credentialStatus ->
    credentialStatus?.let {
        val altText = stringResource(id = credentialStatus.getAltText())
        Box(
            modifier = Modifier
                .heightIn(min = Sizes.labelHeight)
                .clearAndSetSemantics {
                    contentDescription = altText
                }
                .background(
                    color = when (credentialStatus) {
                        CredentialStatus.VALID,
                        CredentialStatus.UNSUPPORTED,
                        CredentialStatus.UNKNOWN -> Color.Transparent
                        CredentialStatus.EXPIRED,
                        CredentialStatus.REVOKED,
                        CredentialStatus.SUSPENDED -> WalletTheme.colorScheme.lightErrorFixed
                    },
                    shape = RoundedCornerShape(Sizes.s04)
                )
                .border(
                    Sizes.line01,
                    color = when (credentialStatus) {
                        CredentialStatus.EXPIRED,
                        CredentialStatus.REVOKED,
                        CredentialStatus.SUSPENDED -> Color.Transparent
                        CredentialStatus.VALID,
                        CredentialStatus.UNSUPPORTED,
                        CredentialStatus.UNKNOWN -> textColor
                    },
                    shape = RoundedCornerShape(Sizes.s04)
                )
                .padding(start = Sizes.s03, end = Sizes.s04),
            contentAlignment = Alignment.Center,
        ) {
            CredentialStatusLabel(credentialStatus, textColor)
        }
    }
}

@Composable
private fun CredentialStatusLabel(
    status: CredentialStatus,
    textColor: Color
) {
    val color = when (status) {
        CredentialStatus.VALID,
        CredentialStatus.UNSUPPORTED,
        CredentialStatus.UNKNOWN -> textColor
        CredentialStatus.EXPIRED,
        CredentialStatus.REVOKED,
        CredentialStatus.SUSPENDED -> WalletTheme.colorScheme.onLightErrorFixed
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = status.getIcon()),
            contentDescription = null,
            tint = color,
        )
        Spacer(modifier = Modifier.size(Sizes.s01))
        Text(
            text = stringResource(id = status.getText()),
            color = color,
            style = WalletTheme.typography.labelMedium,
        )
    }
}

internal fun CredentialStatus.getIcon() = when (this) {
    CredentialStatus.VALID -> R.drawable.wallet_ic_checkmark
    CredentialStatus.EXPIRED,
    CredentialStatus.REVOKED -> R.drawable.wallet_ic_invalid
    CredentialStatus.SUSPENDED -> R.drawable.wallet_ic_front_hand
    CredentialStatus.UNSUPPORTED,
    CredentialStatus.UNKNOWN -> R.drawable.wallet_ic_warning
}

internal fun CredentialStatus.getText() = when (this) {
    CredentialStatus.VALID -> R.string.tk_global_credential_status_valid
    CredentialStatus.UNSUPPORTED,
    CredentialStatus.UNKNOWN -> R.string.tk_global_credential_status_unknown
    CredentialStatus.EXPIRED -> R.string.tk_global_credential_status_expired
    CredentialStatus.REVOKED -> R.string.tk_global_credential_status_revoked
    CredentialStatus.SUSPENDED -> R.string.tk_global_credential_status_suspended
}

internal fun CredentialStatus.getAltText() = when (this) {
    CredentialStatus.VALID -> R.string.tk_credential_status_valid_alt
    CredentialStatus.UNSUPPORTED,
    CredentialStatus.UNKNOWN -> R.string.tk_credential_status_unknown_alt
    CredentialStatus.EXPIRED -> R.string.tk_credential_status_expired_alt
    CredentialStatus.REVOKED -> R.string.tk_credential_status_revoked_alt
    CredentialStatus.SUSPENDED -> R.string.tk_credential_status_suspended_alt
}

private class CredentialStatusProvider : PreviewParameterProvider<CredentialStatus> {
    override val values: Sequence<CredentialStatus> = CredentialStatus.entries.asSequence()
}

@Composable
@WalletComponentPreview
private fun CredentialStatusBadgePreview(@PreviewParameter(CredentialStatusProvider::class) status: CredentialStatus) {
    WalletTheme {
        CredentialStatusBadge(
            status = status,
            textColor = Color.Black
        )
    }
}
