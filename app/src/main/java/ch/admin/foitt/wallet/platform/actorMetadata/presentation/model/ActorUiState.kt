package ch.admin.foitt.wallet.platform.actorMetadata.presentation.model

import androidx.compose.ui.graphics.painter.Painter
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus

data class ActorUiState(
    val name: String?,
    val painter: Painter?,
    val trustStatus: TrustStatus,
    val actorType: ActorType,
) {
    companion object {
        val EMPTY = ActorUiState(
            name = null,
            painter = null,
            trustStatus = TrustStatus.UNKNOWN,
            actorType = ActorType.UNKNOWN,
        )
    }
}
