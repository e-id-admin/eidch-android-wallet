package ch.admin.foitt.wallet.feature.mrzScan.presentation.model

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequest
import kotlinx.serialization.Serializable

@Serializable
data class MrzData(
    val displayName: String,
    val payload: ApplyRequest,
)
