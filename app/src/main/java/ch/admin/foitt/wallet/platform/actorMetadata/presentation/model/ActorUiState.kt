package ch.admin.foitt.wallet.platform.actorMetadata.presentation.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.badges.presentation.model.BadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus

data class ActorUiState(
    val name: String?,
    val painter: Painter?,
    val trustStatus: TrustStatus,
    val vcSchemaTrustStatus: VcSchemaTrustStatus,
    val actorType: ActorType,
    val actorComplianceState: ActorComplianceState,
    val nonComplianceReason: String?,
) {
    companion object {
        val EMPTY = ActorUiState(
            name = null,
            painter = null,
            trustStatus = TrustStatus.UNKNOWN,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            actorType = ActorType.UNKNOWN,
            actorComplianceState = ActorComplianceState.UNKNOWN,
            nonComplianceReason = null,
        )
    }
}

@Composable
fun ActorUiState.actorPainter(): Painter? {
    if (painter != null) {
        return painter
    }
    return when (actorType) {
        ActorType.ISSUER,
        ActorType.VERIFIER -> painterResource(R.drawable.wallet_ic_actor_default)

        ActorType.UNKNOWN -> null
    }
}

fun ActorUiState.toBadgeBottomSheetUiState(onMoreInformation: () -> Unit): BadgeBottomSheetUiState {
    return when (trustStatus) {
        TrustStatus.TRUSTED -> BadgeBottomSheetUiState.TrustVerified(
            actorName = name ?: "",
            actorPainter = painter,
            onMoreInformation = onMoreInformation,
        )
        else -> BadgeBottomSheetUiState.BadgeNotVerified(
            actorName = name ?: "",
            actorPainter = painter,
            onMoreInformation = onMoreInformation
        )
    }
}
