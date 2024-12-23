package ch.admin.foitt.wallet.platform.actorMetadata.presentation.model

import androidx.compose.ui.graphics.painter.Painter
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus

data class ActorUiState(
    val name: String,
    val painter: Painter?,
    val trustStatus: TrustStatus,
) {
    companion object {
        val EMPTY = ActorUiState(
            name = "",
            painter = null,
            trustStatus = TrustStatus.UNKNOWN,
        )
    }
}
