package ch.admin.foitt.sriValidator.domain

import ch.admin.foitt.sriValidator.domain.model.SRIError
import com.github.michaelbull.result.Result

/**
 * Validates the provided data against the given integrity hash-expression (e.g. "sha256-abc123").
 *
 * @param data: The resource data to be validated.
 * @param integrity: The hash-expession integrity value to be checked.
 *
 * @return `Ok(Unit)` if the integrity parameter matches the computed hash of the provided data;
 *         otherwise Err().
 *
 * @return SriError.MalformedIntegrity if the integrity parameter is malformed (e.g. "sha256+abc123" instead of "sha256-abc123").
 * @return SriError.UnsupportedAlgorithm if the integrity hash algorithm is not supported
 * @return SriError.ValidationFailed if the validation did not succeed
 */
interface SRIValidator {
    operator fun invoke(
        data: ByteArray,
        integrity: String,
    ): Result<Unit, SRIError>
}
