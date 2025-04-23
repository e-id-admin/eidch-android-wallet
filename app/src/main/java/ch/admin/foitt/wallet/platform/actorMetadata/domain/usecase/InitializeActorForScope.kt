package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope

internal fun interface InitializeActorForScope {
    suspend operator fun invoke(
        actorDisplayData: ActorDisplayData,
        componentScope: ComponentScope,
    )
}
