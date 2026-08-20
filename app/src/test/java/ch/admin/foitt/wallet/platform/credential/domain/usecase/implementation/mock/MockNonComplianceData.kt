package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceReasonDisplay

object MockNonComplianceData {
    val nonComplianceData = NonComplianceData(
        state = ActorComplianceState.REPORTED,
        reasonDisplays = listOf(
            NonComplianceReasonDisplay(
                locale = "en",
                reason = "reason"
            )
        )
    )
}
