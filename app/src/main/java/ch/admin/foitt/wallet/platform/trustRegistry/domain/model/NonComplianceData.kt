package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

data class NonComplianceData(
    val state: ActorComplianceState,
    val reasonDisplays: List<NonComplianceReasonDisplay>?,
)
