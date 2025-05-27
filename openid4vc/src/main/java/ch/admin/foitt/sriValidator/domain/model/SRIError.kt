package ch.admin.foitt.sriValidator.domain.model

sealed interface SRIError {
    data object MalformedIntegrity : SRIError
    data object ValidationFailed : SRIError
    data class UnsupportedAlgorithm(val algorithm: String) : SRIError
}
