package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase

import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult

fun interface CacheIssuerDisplayData {
    suspend operator fun invoke(
        trustCheckResult: TrustCheckResult?,
        issuerDisplays: List<AnyIssuerDisplay>,
        nonComplianceData: NonComplianceData,
    )
}
