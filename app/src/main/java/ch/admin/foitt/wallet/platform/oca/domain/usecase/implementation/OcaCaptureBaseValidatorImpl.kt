package ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.oca.domain.model.AttributeType
import ch.admin.foitt.wallet.platform.oca.domain.model.CaptureBase
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaCaptureBaseValidationError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaCaptureBaseValidator
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import javax.inject.Inject

class OcaCaptureBaseValidatorImpl @Inject constructor() : OcaCaptureBaseValidator {

    override suspend fun invoke(
        captureBases: List<CaptureBase>
    ): Result<List<CaptureBase>, OcaCaptureBaseValidationError> =
        coroutineBinding {
            val rootCaptureBases = findRootCaptureBases(captureBases)
            if (rootCaptureBases.size != 1) {
                return@coroutineBinding Err(OcaError.InvalidRootCaptureBase).bind<List<CaptureBase>>()
            }

            if (doCaptureBasesContainInvalidReferences(captureBases)) {
                return@coroutineBinding Err(OcaError.InvalidCaptureBaseReferenceAttribute).bind<List<CaptureBase>>()
            }

            val rootCaptureBase = rootCaptureBases.first()
            if (doCaptureBasesContainReferenceCycles(captureBases, rootCaptureBase, listOf(rootCaptureBase.digest))) {
                return@coroutineBinding Err(OcaError.CaptureBaseCycleError).bind<List<CaptureBase>>()
            }

            captureBases
        }

    private fun findRootCaptureBases(captureBases: List<CaptureBase>): List<CaptureBase> {
        // A Capture Base is called the root Capture Base, if it isn't referenced by any other Capture Base.
        val rootCaptureBases = captureBases.filter { captureBase ->
            val captureBasesAttributes = captureBases.flatMap { it.attributes.values }
            captureBasesAttributes
                .mapNotNull { getReferenceAttribute(it) }
                .none { captureBaseReference ->
                    captureBaseReference == captureBase.digest
                }
        }
        return rootCaptureBases
    }

    private fun doCaptureBasesContainInvalidReferences(captureBases: List<CaptureBase>): Boolean {
        val allAttributes = captureBases.flatMap { it.attributes.values }
        val referenceAttributes = allAttributes.mapNotNull { getReferenceAttribute(it) }
        val captureBaseDigests = captureBases.map { it.digest }

        return referenceAttributes.any { !captureBaseDigests.contains(it) }
    }

    private fun getReferenceAttribute(attributeType: AttributeType): String? = when (attributeType) {
        is AttributeType.Reference -> attributeType.captureBaseReference
        is AttributeType.Array -> getReferenceAttribute(attributeType.attributeType)
        else -> null
    }

    @Suppress("ReturnCount")
    private fun doCaptureBasesContainReferenceCycles(
        captureBases: List<CaptureBase>,
        captureBase: CaptureBase,
        referencedCaptureBaseDigests: List<String>
    ): Boolean {
        val nextCaptureBaseDigests = captureBase.attributes.values.mapNotNull { getReferenceAttribute(it) }
        val nextCaptureBases = captureBases.filter { it.digest in nextCaptureBaseDigests }
        nextCaptureBases.forEach { nextCaptureBase ->
            if (!referencedCaptureBaseDigests.contains(nextCaptureBase.digest)) {
                val containsCycle = doCaptureBasesContainReferenceCycles(
                    captureBases = captureBases,
                    captureBase = nextCaptureBase,
                    referencedCaptureBaseDigests = referencedCaptureBaseDigests + listOf(nextCaptureBase.digest)
                )
                if (containsCycle) return true
            } else {
                return true
            }
        }

        return false
    }
}
