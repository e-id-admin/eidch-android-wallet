package ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter

import androidx.annotation.StringRes
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState

fun interface GetActorUiState {
    suspend operator fun invoke(
        actorDisplayData: ActorDisplayData,
        @StringRes defaultName: Int,
    ): ActorUiState
}
