package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GuardianVerificationResponse(
    @SerialName("legalRepresentantVerificationRequestUrl")
    val legalRepresentantVerificationRequestUrl: String,
    @SerialName("legalRepresentantVerifierLink")
    val legalRepresentantVerifierLink: String,
)
