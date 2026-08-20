package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyVcSdJwtBatchConsistencyError
import com.github.michaelbull.result.Result

internal interface VerifyVcSdJwtBatchConsistency {
    /**
     * Enforces that all credentials of a single issuance batch share the same credential format and
     * dataset (user claims), but contain different cryptographic data (confirmation key, status and
     * disclosure salts).
     */
    @CheckResult
    operator fun invoke(
        credentials: List<VcSdJwtCredential>,
    ): Result<Unit, VerifyVcSdJwtBatchConsistencyError>
}
