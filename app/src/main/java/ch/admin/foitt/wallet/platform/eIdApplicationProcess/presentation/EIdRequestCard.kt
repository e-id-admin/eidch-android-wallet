package ch.admin.foitt.wallet.platform.eIdApplicationProcess.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialCardVerySmallSquare
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun EIdRequestCard(
    eIdRequest: EIdRequest,
    onStartOnlineIdentification: () -> Unit,
) = when (eIdRequest.state) {
    // state unknown
    null -> {}

    EIdRequestQueueState.IN_QUEUING -> EIdRequestCardGeneric(
        title = stringResource(
            R.string.tk_getEid_notification_eidProgress_title_android,
            "${eIdRequest.firstName} ${eIdRequest.lastName}"
        ),
        body = stringResource(
            R.string.tk_getEid_notification_eidProgress_body_android,
            eIdRequest.onlineSessionStartOpenAt ?: "" // this can not be null here
        )
    )

    EIdRequestQueueState.READY_FOR_ONLINE_SESSION -> EIdRequestCardGeneric(
        title = stringResource(
            R.string.tk_getEid_notification_eidReady_title_android,
            "${eIdRequest.firstName} ${eIdRequest.lastName}"
        ),
        body = stringResource(
            R.string.tk_getEid_notification_eidReady_body_android,
            eIdRequest.onlineSessionStartTimeoutAt ?: "" // this can not be null here
        ),
        buttonText = stringResource(R.string.tk_getEid_notification_eidReady_greenButton),
        onButtonClick = onStartOnlineIdentification,
    )

    EIdRequestQueueState.CLOSED,
    EIdRequestQueueState.IN_TARGET_WALLET_PAIRING,
    EIdRequestQueueState.IN_AUTO_VERIFICATION,
    EIdRequestQueueState.READY_FOR_FINAL_ENTITLEMENT_CHECK,
    EIdRequestQueueState.IN_ISSUANCE,
    EIdRequestQueueState.REFUSED,
    EIdRequestQueueState.CANCELLED,
    EIdRequestQueueState.TIMEOUT -> {
    }
}

@Composable
fun EIdRequestCardGeneric(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    buttonText: String? = null,
    useTertiaryButton: Boolean = true,
    onButtonClick: () -> Unit = {},
    onCloseClick: (() -> Unit)? = null,
) = Surface(
    color = WalletTheme.colorScheme.lightPrimary,
    shape = RoundedCornerShape(Sizes.s05),
) {
    Row(
        modifier = modifier.padding(Sizes.s06),
    ) {
        CredentialCardVerySmallSquare()
        Spacer(modifier = Modifier.width(Sizes.s04))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            WalletTexts.TitleSmall(
                modifier = Modifier.semantics {
                    traversalIndex = -4f
                },
                text = title
            )
            WalletTexts.LabelLarge(
                modifier = Modifier.semantics {
                    traversalIndex = -3f
                },
                text = body
            )
            buttonText?.let {
                Spacer(modifier = Modifier.height(Sizes.s04))
                if (useTertiaryButton) {
                    Buttons.FilledTertiary(
                        modifier = Modifier.semantics {
                            traversalIndex = -2f
                        },
                        text = buttonText,
                        onClick = onButtonClick,
                    )
                } else {
                    Buttons.Text(
                        modifier = Modifier.semantics {
                            traversalIndex = -2f
                        },
                        text = buttonText,
                        startIcon = painterResource(R.drawable.wallet_ic_pull_to_refresh),
                        enabled = false,
                        onClick = onButtonClick,
                    )
                }
            }
        }
        onCloseClick?.let {
            Spacer(modifier = Modifier.width(Sizes.s04))
            IconButton(
                modifier = Modifier
                    .size(Sizes.s08)
                    .background(
                        color = WalletTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    )
                    .padding(Sizes.s01)
                    .spaceBarKeyClickable(onCloseClick),
                onClick = onCloseClick,
            ) {
                Icon(

                    painter = painterResource(id = R.drawable.wallet_ic_cross),
                    contentDescription = "close",
                    tint = WalletTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

private class EIdRequestCardPreviewParams : PreviewParameterProvider<EIdRequest> {
    override val values: Sequence<EIdRequest> = sequenceOf(
        getEIdRequestForPreview(null),
        getEIdRequestForPreview(EIdRequestQueueState.IN_QUEUING),
        getEIdRequestForPreview(EIdRequestQueueState.READY_FOR_ONLINE_SESSION),
        getEIdRequestForPreview(EIdRequestQueueState.IN_TARGET_WALLET_PAIRING),
        getEIdRequestForPreview(EIdRequestQueueState.IN_AUTO_VERIFICATION),
        getEIdRequestForPreview(EIdRequestQueueState.READY_FOR_FINAL_ENTITLEMENT_CHECK),
        getEIdRequestForPreview(EIdRequestQueueState.IN_ISSUANCE),
        getEIdRequestForPreview(EIdRequestQueueState.REFUSED),
        getEIdRequestForPreview(EIdRequestQueueState.CANCELLED),
        getEIdRequestForPreview(EIdRequestQueueState.TIMEOUT),
        getEIdRequestForPreview(EIdRequestQueueState.CLOSED),
    )
}

private fun getEIdRequestForPreview(queueingState: EIdRequestQueueState?) = EIdRequest(
    state = queueingState,
    firstName = "Seraina",
    lastName = "Muster",
    onlineSessionStartOpenAt = "06.02.2025",
    onlineSessionStartTimeoutAt = "08.02.2025",
)

@WalletComponentPreview
@Composable
private fun EIdRequestCardPreview(
    @PreviewParameter(EIdRequestCardPreviewParams::class) eIdRequest: EIdRequest,
) {
    WalletTheme {
        EIdRequestCard(
            eIdRequest = eIdRequest,
            onStartOnlineIdentification = {},
        )
    }
}
