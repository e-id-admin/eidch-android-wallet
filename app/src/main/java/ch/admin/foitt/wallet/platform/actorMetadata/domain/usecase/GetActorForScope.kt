package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import kotlinx.coroutines.flow.StateFlow

fun interface GetActorForScope {
    operator fun invoke(
        componentScope: ComponentScope,
    ): StateFlow<ActorDisplayData>
}
