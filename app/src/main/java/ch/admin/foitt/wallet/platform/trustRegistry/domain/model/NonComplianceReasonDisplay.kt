package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import kotlinx.serialization.Serializable

@Serializable
data class NonComplianceReasonDisplay(
    override val locale: String,
    val reason: String,
) : LocalizedDisplay
