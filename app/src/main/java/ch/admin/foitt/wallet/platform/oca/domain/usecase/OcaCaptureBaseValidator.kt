package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaCaptureBaseValidationError
import com.github.michaelbull.result.Result

interface OcaCaptureBaseValidator {
    suspend operator fun invoke(captureBases: List<CaptureBase>): Result<List<CaptureBase>, OcaCaptureBaseValidationError>
}
