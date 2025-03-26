package ch.admin.foitt.sriValidator.domain

/**
 * Validates the provided data against the given integrity hash-expression (e.g. "sha256-abc123").
 *
 * @param data: The resource data to be validated.
 * @param integrity: The hash-expession integrity value to be checked.
 *
 * @return `true` if the integrity parameter matches the computed hash of the provided data;
 *         otherwise `false`.
 *
 * @throws SriError.MalformedIntegrity if the integrity parameter is malformed (e.g. "sha256+abc123" instead of "sha256-abc123").
 * @throws SriError.UnsupportedAlgorithm if the integrity hash algorithm is not supported
 */
interface SRIValidator {
    fun validate(
        data: ByteArray,
        integrity: String,
    ): Boolean
}
