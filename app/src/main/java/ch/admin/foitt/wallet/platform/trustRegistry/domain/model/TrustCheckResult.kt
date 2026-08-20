package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

data class TrustCheckResult(
    val identityTrustStatement: IdentityTrustStatement?,
    val vcSchemaTrustStatus: VcSchemaTrustStatus,
    val nonComplianceData: NonComplianceData
)
