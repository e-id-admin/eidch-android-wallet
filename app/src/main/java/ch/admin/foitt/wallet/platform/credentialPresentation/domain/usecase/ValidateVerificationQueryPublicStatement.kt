package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateVqPsError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement
import com.github.michaelbull.result.Result

interface ValidateVerificationQueryPublicStatement {
    suspend operator fun invoke(
        jwt: Jwt,
        actorDid: String,
    ): Result<VerificationQueryPublicStatement, ValidateVqPsError>
}
