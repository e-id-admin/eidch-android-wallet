package ch.admin.foitt.wallet.platform.userInteraction.data.repository

import ch.admin.foitt.wallet.platform.userInteraction.domain.model.UserInteraction
import ch.admin.foitt.wallet.platform.userInteraction.domain.repository.UserInteractionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class UserInteractionRepositoryImpl @Inject constructor() : UserInteractionRepository {

    private val _lastInteraction = MutableStateFlow(UserInteraction())
    override val lastInteraction = _lastInteraction.asStateFlow()

    override fun updateLastInteraction() {
        _lastInteraction.value = UserInteraction()
    }
}
