package ch.admin.foitt.wallet.platform.actorMetadata.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.actorPainter
import ch.admin.foitt.wallet.platform.composables.Avatar
import ch.admin.foitt.wallet.platform.composables.AvatarSize
import ch.admin.foitt.wallet.platform.composables.Callouts
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toIcon
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InvitationHeader(
    actorUiState: ActorUiState,
    onActorNameTap: () -> Unit,
    onReportedActorInfo: () -> Unit,
    modifier: Modifier = Modifier,
) = Card(
    shape = RoundedCornerShape(bottomStart = Sizes.s09, bottomEnd = Sizes.s09),
    colors = CardDefaults.cardColors(containerColor = WalletTheme.colorScheme.surface)
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(start = Sizes.s04, end = Sizes.s04, top = Sizes.s04, bottom = Sizes.s02),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onActorNameTap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                imagePainter = actorUiState.actorPainter(),
                size = AvatarSize.LARGE,
                imageTint = WalletTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.size(Sizes.s04))
            WalletTexts.TitleMedium(
                text = actorUiState.name ?: fallBackName(actorUiState.actorType),
                color = WalletTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .semantics { heading() }
            )
            actorUiState.trustStatus.toIcon()?.let {
                Spacer(modifier = Modifier.size(Sizes.s04))
                Icon(
                    modifier = Modifier.size(Sizes.s04),
                    painter = painterResource(it),
                    tint = WalletTheme.colorScheme.onLightTertiary,
                    contentDescription = null,
                )
            }
            Spacer(modifier = Modifier.size(Sizes.s02))
        }
        if (actorUiState.actorComplianceState == ActorComplianceState.REPORTED) {
            Spacer(modifier = Modifier.height(Sizes.s06))
            Callouts.ReportedActor(
                modifier = Modifier.padding(horizontal = Sizes.s04),
                label = R.string.tk_actor_nonCompliant_button,
                onClick = onReportedActorInfo,
            )
        }
        Spacer(modifier = Modifier.height(Sizes.s04))
    }
}

@Composable
private fun fallBackName(actorType: ActorType): String = when (actorType) {
    ActorType.ISSUER -> stringResource(R.string.tk_credential_offer_issuer_name_unknown)
    ActorType.VERIFIER -> stringResource(R.string.presentation_verifier_name_unknown)
    ActorType.UNKNOWN -> ""
}

private data class InvitationHeaderPreviewParam(
    val actorName: String?,
    val actorLogo: Int,
    val trustStatus: TrustStatus,
    val vcSchemaTrustStatus: VcSchemaTrustStatus,
    val actorComplianceState: ActorComplianceState,
)

private class InvitationHeaderPreviewParams : PreviewParameterProvider<InvitationHeaderPreviewParam> {
    override val values: Sequence<InvitationHeaderPreviewParam> = sequenceOf(
        InvitationHeaderPreviewParam(
            actorName = "Issuer Name",
            actorLogo = R.drawable.wallet_ic_eid,
            trustStatus = TrustStatus.TRUSTED,
            vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
            actorComplianceState = ActorComplianceState.REPORTED,
        ),
        InvitationHeaderPreviewParam(
            actorName = "Issuer Name",
            actorLogo = R.drawable.wallet_ic_eid,
            trustStatus = TrustStatus.TRUSTED,
            vcSchemaTrustStatus = VcSchemaTrustStatus.NOT_TRUSTED,
            actorComplianceState = ActorComplianceState.NOT_REPORTED,
        ),
        InvitationHeaderPreviewParam(
            actorName = "Issuer with a veeeeryyyyy loooonnnnnng name",
            actorLogo = R.drawable.ic_launcher_background,
            trustStatus = TrustStatus.TRUSTED,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            actorComplianceState = ActorComplianceState.NOT_REPORTED,
        ),
        InvitationHeaderPreviewParam(
            actorName = "Issuer Name not trusted",
            actorLogo = R.drawable.wallet_ic_actor_default,
            trustStatus = TrustStatus.NOT_TRUSTED,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            actorComplianceState = ActorComplianceState.NOT_REPORTED,
        ),
        InvitationHeaderPreviewParam(
            actorName = "Issuer Name trust unknown",
            actorLogo = R.drawable.wallet_ic_dotted_cross,
            trustStatus = TrustStatus.UNKNOWN,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            actorComplianceState = ActorComplianceState.NOT_REPORTED,
        ),
        InvitationHeaderPreviewParam(
            actorName = null,
            actorLogo = R.drawable.wallet_ic_dotted_cross,
            trustStatus = TrustStatus.UNKNOWN,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            actorComplianceState = ActorComplianceState.UNKNOWN,
        ),
    )
}

@WalletComponentPreview
@Composable
private fun InvitationHeaderPreview(
    @PreviewParameter(InvitationHeaderPreviewParams::class) previewParams: InvitationHeaderPreviewParam,
) {
    WalletTheme {
        InvitationHeader(
            actorUiState = ActorUiState(
                name = previewParams.actorName,
                painter = painterResource(previewParams.actorLogo),
                trustStatus = previewParams.trustStatus,
                vcSchemaTrustStatus = previewParams.vcSchemaTrustStatus,
                actorType = ActorType.ISSUER,
                actorComplianceState = previewParams.actorComplianceState,
                nonComplianceReason = null,
            ),
            onActorNameTap = { },
            onReportedActorInfo = {},
        )
    }
}
