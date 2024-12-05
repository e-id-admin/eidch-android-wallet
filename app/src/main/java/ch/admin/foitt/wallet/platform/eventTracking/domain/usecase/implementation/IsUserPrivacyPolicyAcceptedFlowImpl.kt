package ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eventTracking.domain.repository.UserPrivacyPolicyRepository
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.IsUserPrivacyPolicyAcceptedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class IsUserPrivacyPolicyAcceptedFlowImpl @Inject constructor(
    private val userPrivacyPolicyRepository: UserPrivacyPolicyRepository,
) : IsUserPrivacyPolicyAcceptedFlow {
    override fun invoke(): StateFlow<Boolean> = userPrivacyPolicyRepository.isUserPrivacyPolicyAcceptedFlow
}
