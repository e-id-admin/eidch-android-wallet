package ch.admin.foitt.wallet.platform.actorMetadata.data.repository

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.repository.ActorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ActorRepositoryImpl @Inject constructor() : ActorRepository {
    private val _actorDisplayData = MutableStateFlow<ActorDisplayData>(ActorDisplayData.EMPTY)
    override val actorDisplayData = _actorDisplayData.asStateFlow()

    override fun setActor(actor: ActorDisplayData) {
        _actorDisplayData.update { actor }
    }
}
