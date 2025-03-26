package ch.admin.foitt.sriValidator.domain.model

internal sealed class SRIError(message: String) : Exception(message) {
    object MalformedIntegrity : SRIError("The integrity parameter is malformed.")
    data class UnsupportedAlgorithm(val algorithm: String) :
        SRIError("Unsupported algorithm: $algorithm")
}
