package ch.admin.foitt.wallet.platform.eIdApplicationProcess.presentation.mock

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplyResponseBody(
    @SerialName("caseId")
    val caseId: String,
    @SerialName("surname")
    val surname: String,
    @SerialName("givenNames")
    val givenNames: String,
    @SerialName("dateOfBirth")
    val dateOfBirth: String,
    @SerialName("identityType")
    val identityType: String,
    @SerialName("identityNumber")
    val identityNumber: String,
    @SerialName("validUntil")
    val validUntil: String,
    @SerialName("legalRepresentant")
    val legalRepresentant: Boolean,
    @SerialName("email")
    val email: String,
)
