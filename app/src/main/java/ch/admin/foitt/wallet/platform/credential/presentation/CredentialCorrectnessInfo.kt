package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletCards
import ch.admin.foitt.wallet.theme.WalletTexts

@Composable
fun CredentialCorrectnessInfo(icon: Painter) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Sizes.s08, vertical = Sizes.s04)
    ) {
        WalletCards.InfoCard {
            ConstraintLayout(
                modifier = Modifier.padding(horizontal = Sizes.s05, vertical = Sizes.s01),
            ) {
                val (imageRef, textRef) = createRefs()

                Icon(
                    modifier = Modifier
                        .constrainAs(imageRef) {
                            top.linkTo(textRef.top)
                            start.linkTo(parent.start)
                            end.linkTo(textRef.start)
                        }
                        .padding(end = Sizes.s02),
                    painter = icon,
                    contentDescription = null
                )
                WalletTexts.InfoLabel(
                    modifier = Modifier.constrainAs(textRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(imageRef.end)
                        end.linkTo(parent.end)
                    },
                    text = stringResource(id = R.string.credential_offer_support_message)
                )
            }
        }
    }
}
