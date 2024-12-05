package ch.admin.foitt.wallet.feature.sessionTimeout.domain.implementation

import ch.admin.foitt.wallet.feature.sessionTimeout.domain.UserInteractionFlow
import ch.admin.foitt.wallet.platform.userInteraction.domain.model.UserInteraction
import ch.admin.foitt.wallet.platform.userInteraction.domain.repository.UserInteractionRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class UserInteractionFlowImpl @Inject constructor(
    private val userInteractionRepository: UserInteractionRepository,
) : UserInteractionFlow {
    override suspend fun invoke(): StateFlow<UserInteraction> = userInteractionRepository.lastInteraction
}
