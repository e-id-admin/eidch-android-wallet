package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IsTrustedDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_JTI
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_PROFILE_VERSION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementJwtError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementStatusError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateVerificationAuthorizationTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationAuthorizationTrustStatement.Companion.CLAIM_NAME_AUTHORIZED_FIELDS
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationAuthorizationTrustStatement.Companion.TYPE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toUnexpected
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toValidateVerificationAuthorizationTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateVerificationAuthorizationTrustStatement
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class ValidateVerificationAuthorizationTrustStatementImpl @Inject constructor(
    private val validateTrustStatementJwt: ValidateTrustStatementJwt,
    private val validateTrustStatementStatus: ValidateTrustStatementStatus,
    private val isTrustedDid: IsTrustedDid,
) : ValidateVerificationAuthorizationTrustStatement {
    override suspend fun invoke(
        trustStatement: Jwt,
        actorDid: String
    ): Result<VerificationAuthorizationTrustStatement, ValidateVerificationAuthorizationTrustStatementError> = coroutineBinding {
        validateTrustStatementJwt(
            trustStatement = trustStatement
        ).mapError(ValidateTrustStatementJwtError::toValidateVerificationAuthorizationTrustStatementError)
            .bind()

        runSuspendCatching {
            val type = checkNotNull(trustStatement.type) { "type must not be null" }
            check(type == TYPE) { "type is not protected verification authorization trust statement" }

            val sub = checkNotNull(trustStatement.subject) { "sub must not be null" }
            check(sub == actorDid)

            val trustStatementStatusResult = validateTrustStatementStatus(
                trustStatement = trustStatement,
            ).mapError(ValidateTrustStatementStatusError::toValidateVerificationAuthorizationTrustStatementError)
                .bind()

            isTrustedDid(
                keyId = trustStatementStatusResult.kid,
                trustStatementType = TYPE,
            ).mapError(IsTrustedDidError::toValidateVerificationAuthorizationTrustStatementError)
                .bind()

            VerificationAuthorizationTrustStatement(
                typ = type,
                alg = SignatureAlgorithm.fromStdNameOrThrow(trustStatement.algorithm),
                kid = trustStatementStatusResult.kid,
                profileVersion = trustStatement.signedJwt.header.getCustomParam(CLAIM_NAME_PROFILE_VERSION).toString(),
                jti = trustStatement.signedJwt.jwtClaimsSet.getStringClaim(CLAIM_NAME_JTI),
                iat = trustStatement.issuedAt!!.epochSecond,
                exp = trustStatement.expInstant!!.epochSecond,
                sub = sub,
                status = trustStatementStatusResult.status,
                authorizedFields = getAuthorizedFields(trustStatement)
            )
        }.mapError(Throwable::toUnexpected).bind()
    }

    private fun getAuthorizedFields(trustStatement: Jwt): Set<String> {
        val claimsSet = trustStatement.signedJwt.jwtClaimsSet
        val fields = claimsSet.getListClaim(CLAIM_NAME_AUTHORIZED_FIELDS)
        check(!fields.isNullOrEmpty()) { "authorized_fields is missing" }
        return fields.map { it as String }
            .toSet()
    }
}
