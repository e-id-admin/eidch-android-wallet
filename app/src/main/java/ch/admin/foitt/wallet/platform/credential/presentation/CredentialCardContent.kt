package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts

@Composable
fun CredentialCardContent(
    credentialCardState: CredentialCardState,
    modifier: Modifier = Modifier,
) = ConstraintLayout(
    modifier = modifier.fillMaxSize()
) {
    val (
        titleRef,
        subtitleRef,
        statusRef,
    ) = createRefs()
    credentialCardState.title?.let {
        WalletTexts.MediumCredentialTitle(
            text = credentialCardState.title,
            color = credentialCardState.contentColor,
            maxLines = 3,
            modifier = Modifier
                .constrainAs(titleRef) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(statusRef.top)
                    width = Dimension.fillToConstraints
                }
        )
    }
    credentialCardState.subtitle?.let {
        WalletTexts.MediumCredentialSubtitle(
            text = credentialCardState.subtitle,
            color = credentialCardState.contentColor,
            maxLines = 3,
            modifier = Modifier
                .constrainAs(subtitleRef) {
                    start.linkTo(parent.start)
                    end.linkTo(statusRef.start, margin = Sizes.s02)
                    top.linkTo(titleRef.bottom)
                    width = Dimension.fillToConstraints
                }
        )
    }

    CredentialStatusBadge(
        status = credentialCardState.status,
        textColor = credentialCardState.contentColor,
        modifier = Modifier.constrainAs(statusRef) {
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
        }
    )
}
