package ch.admin.foitt.wallet.platform.eventTracking.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface UserPrivacyPolicyRepository {
    val isUserPrivacyPolicyAcceptedFlow: StateFlow<Boolean>

    fun applyUserPrivacyPolicy(hasAccepted: Boolean)
}
