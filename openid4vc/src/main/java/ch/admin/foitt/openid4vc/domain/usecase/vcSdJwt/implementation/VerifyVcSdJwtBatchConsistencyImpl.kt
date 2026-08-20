package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyVcSdJwtBatchConsistencyError
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtBatchConsistency
import ch.admin.foitt.swiyu.shared.consistency.SdJwtCredentialConsistencyChecker
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import javax.inject.Inject

internal class VerifyVcSdJwtBatchConsistencyImpl @Inject constructor(
    private val consistencyChecker: SdJwtCredentialConsistencyChecker,
) : VerifyVcSdJwtBatchConsistency {
    override fun invoke(
        credentials: List<VcSdJwtCredential>,
    ): Result<Unit, VerifyVcSdJwtBatchConsistencyError> {
        val reference = credentials.firstOrNull() ?: return Ok(Unit)

        // Drop the first credential (reference) and check consistency
        // for the rest of the credentials in the batch
        val isConsistent = credentials.drop(1).all { credential ->
            consistencyChecker.checkConsistency(
                left = reference.payload,
                right = credential.payload,
            ) == SdJwtCredentialConsistencyChecker.ConsistencyResult.Ok
        }

        return if (isConsistent) {
            Ok(Unit)
        } else {
            Err(VcSdJwtError.BatchConsistencyValidationFailed)
        }
    }
}
