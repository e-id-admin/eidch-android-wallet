package ch.admin.foitt.openid4vc.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import com.github.michaelbull.result.Result
import com.nimbusds.jwt.SignedJWT

interface VerifyJwtSignature {
    @CheckResult
    suspend operator fun invoke(issuerDid: String, signedJwt: SignedJWT): Result<Unit, VerifyJwtError>
}
