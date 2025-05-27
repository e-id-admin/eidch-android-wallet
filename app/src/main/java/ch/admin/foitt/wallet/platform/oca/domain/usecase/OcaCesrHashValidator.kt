package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.wallet.platform.oca.domain.model.OcaCesrHashValidatorError
import com.github.michaelbull.result.Result

/**
 * Validates the CESR hash of the provided OCA object.
 *
 * @param ocaObjectJson The resource data to be validated.
 *
 * @return
 * `Ok(Unit)` if the integrity parameter matches the computed hash of the provided data.
 *
 * `Err(OcaCesrHashValidatorError)` otherwise.
 *
 */
interface OcaCesrHashValidator {
    suspend operator fun invoke(ocaObjectJson: String): Result<Unit, OcaCesrHashValidatorError>
}
