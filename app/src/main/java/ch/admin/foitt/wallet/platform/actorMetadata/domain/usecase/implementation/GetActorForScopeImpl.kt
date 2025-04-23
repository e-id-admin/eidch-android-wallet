package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.actorMetadata.di.ActorRepositoryEntryPoint
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.GetActorForScope
import ch.admin.foitt.wallet.platform.navigation.DestinationScopedComponentManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

internal class GetActorForScopeImpl @Inject constructor(
    private val destinationScopedComponentManager: DestinationScopedComponentManager,
) : GetActorForScope {
    override operator fun invoke(
        componentScope: ComponentScope,
    ): StateFlow<ActorDisplayData> {
        val actorRepository = destinationScopedComponentManager.getEntryPoint(
            entryPointClass = ActorRepositoryEntryPoint::class.java,
            componentScope = componentScope,
        ).actorRepository()
        return actorRepository.actorDisplayData
    }
}
