package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresentationRequestBody(
    @SerialName("vp_token")
    val vpToken: String,
    @SerialName("presentation_submission")
    val presentationSubmission: PresentationSubmission
)

@Serializable
data class PresentationRequestErrorBody(
    @SerialName("error")
    val error: ErrorType,
    @SerialName("error_description")
    val errorDescription: String? = null,
) {
    enum class ErrorType(val key: String) {
        CLIENT_REJECTED("client_rejected"),
        INVALID_REQUEST("invalid_request")
    }
}

@Serializable
data class PresentationSubmission(
    @SerialName("definition_id")
    val definitionId: String,
    @SerialName("descriptor_map")
    val descriptorMap: List<DescriptorMap>,
    @SerialName("id")
    val id: String
)

@Serializable
data class DescriptorMap(
    @SerialName("format")
    val format: String,
    @SerialName("id")
    val id: String,
    @SerialName("path")
    val path: String,
)
