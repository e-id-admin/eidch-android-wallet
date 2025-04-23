package ch.admin.foitt.wallet.platform.actorMetadata.domain.repository

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import kotlinx.coroutines.flow.StateFlow

interface ActorRepository {
    val actorDisplayData: StateFlow<ActorDisplayData>
    fun setActor(actor: ActorDisplayData)
}
