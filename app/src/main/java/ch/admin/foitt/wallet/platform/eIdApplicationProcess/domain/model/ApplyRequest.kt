package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ApplyRequest(
    val mrz: List<String>,
    val legalRepresentant: Boolean = false,
    val email: String? = null,
)
