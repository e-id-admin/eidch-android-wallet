package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceData

interface CheckActorCompliance {
    suspend operator fun invoke(actorDid: String): NonComplianceData
}
