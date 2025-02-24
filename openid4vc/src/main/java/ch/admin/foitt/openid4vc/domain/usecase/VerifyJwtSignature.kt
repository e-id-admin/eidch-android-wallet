package ch.admin.foitt.openid4vc.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import com.github.michaelbull.result.Result

interface VerifyJwtSignature {
    @CheckResult
    suspend operator fun invoke(did: String, kid: String, jwt: Jwt): Result<Unit, VerifyJwtError>
}
