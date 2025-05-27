package ch.admin.foitt.wallet.platform.navArgs.domain.model

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianConsentResultState

data class EIdGuardianConsentResultNavArg(
    val screenState: GuardianConsentResultState,
    val rawDeadline: String?,
)
