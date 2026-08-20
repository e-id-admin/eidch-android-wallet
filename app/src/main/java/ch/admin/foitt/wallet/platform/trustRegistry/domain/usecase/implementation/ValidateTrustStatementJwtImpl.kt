package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureFromDidError
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_JTI
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_PROFILE_VERSION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementJwtError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toUnexpected
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toValidateTrustStatementJwtError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class ValidateTrustStatementJwtImpl @Inject constructor(
    private val verifyJwtSignatureFromDid: VerifyJwtSignatureFromDid,
) : ValidateTrustStatementJwt {
    override suspend fun invoke(
        trustStatement: Jwt,
    ): Result<Unit, ValidateTrustStatementJwtError> = coroutineBinding {
        runSuspendCatching {
            SignatureAlgorithm.fromStdNameOrThrow(trustStatement.algorithm)

            val kid = checkNotNull(trustStatement.keyId) { "keyId must not be null" }
            verifyJwtSignatureFromDid(
                kid = kid,
                jwt = trustStatement,
            ).mapError(VerifyJwtSignatureFromDidError::toValidateTrustStatementJwtError)
                .bind()

            val profileVersion = trustStatement.signedJwt.header.getCustomParam(CLAIM_NAME_PROFILE_VERSION).toString()
            check(profileVersion.startsWith(PROFILE_VERSION_PREFIX)) { "profile_version has incorrect prefix" }

            val jti = trustStatement.signedJwt.jwtClaimsSet.getStringClaim(CLAIM_NAME_JTI)
            check(uuidV4Regex.matches(jti)) { "jti is not a UUID v4" }

            checkNotNull(trustStatement.issuedAt) { "iat must not be null" }
            checkNotNull(trustStatement.expInstant) { "exp must not be null" }
            check(trustStatement.jwtValidity == Validity.Valid)
        }.mapError(Throwable::toUnexpected).bind()
    }

    private companion object {
        const val PROFILE_VERSION_PREFIX = "swiss-profile-trust:"
        val uuidV4Regex = Regex(
            "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
            RegexOption.IGNORE_CASE
        )
    }
}
