package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaCaptureBaseValidationError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.getReferenceValue
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCaptureBaseValidator
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import javax.inject.Inject

class OcaCaptureBaseValidatorImpl @Inject constructor() : OcaCaptureBaseValidator {

    override suspend fun invoke(
        captureBases: List<CaptureBase>
    ): Result<List<CaptureBase>, OcaCaptureBaseValidationError> = coroutineBinding {
        if (doCaptureBasesContainInvalidReferences(captureBases)) {
            return@coroutineBinding Err(OcaError.InvalidCaptureBaseReferenceAttribute).bind<List<CaptureBase>>()
        }

        captureBases
    }

    private fun doCaptureBasesContainInvalidReferences(captureBases: List<CaptureBase>): Boolean {
        val allAttributes = captureBases.flatMap { it.attributes.values }
        val referenceAttributes = allAttributes.mapNotNull { it.getReferenceValue() }
        val captureBaseDigests = captureBases.map { it.digest }

        return referenceAttributes.any { !captureBaseDigests.contains(it) }
    }
}
