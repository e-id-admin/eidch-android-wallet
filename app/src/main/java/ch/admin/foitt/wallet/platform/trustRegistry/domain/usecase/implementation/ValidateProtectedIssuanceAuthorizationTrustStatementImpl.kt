package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IsTrustedDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceAuthorizationObject
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceAuthorizationTrustStatement.Companion.CLAIM_NAME_CAN_ISSUE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceAuthorizationTrustStatement.Companion.TYPE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_JTI
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_PROFILE_VERSION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateProtectedIssuanceAuthorizationTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementJwtError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementStatusError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toUnexpected
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toValidateProtectedIssuanceAuthorizationTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateProtectedIssuanceAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementStatus
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class ValidateProtectedIssuanceAuthorizationTrustStatementImpl @Inject constructor(
    private val validateTrustStatementJwt: ValidateTrustStatementJwt,
    private val validateTrustStatementStatus: ValidateTrustStatementStatus,
    private val isTrustedDid: IsTrustedDid,
    private val safeJson: SafeJson,
) : ValidateProtectedIssuanceAuthorizationTrustStatement {
    override suspend fun invoke(
        trustStatement: Jwt,
        actorDid: String,
    ): Result<ProtectedIssuanceAuthorizationTrustStatement, ValidateProtectedIssuanceAuthorizationTrustStatementError> = coroutineBinding {
        validateTrustStatementJwt(
            trustStatement = trustStatement
        ).mapError(ValidateTrustStatementJwtError::toValidateProtectedIssuanceAuthorizationTrustStatementError)
            .bind()

        runSuspendCatching {
            val typ = checkNotNull(trustStatement.type) { "type must not be null" }
            check(typ == TYPE) { "type is not protected issuance trust list statement" }

            val sub = checkNotNull(trustStatement.subject) { "sub must not be null" }
            check(sub == actorDid)

            val trustStatementStatusResult = validateTrustStatementStatus(
                trustStatement = trustStatement,
            ).mapError(ValidateTrustStatementStatusError::toValidateProtectedIssuanceAuthorizationTrustStatementError)
                .bind()

            isTrustedDid(
                keyId = trustStatementStatusResult.kid,
                trustStatementType = TYPE,
            ).mapError(IsTrustedDidError::toValidateProtectedIssuanceAuthorizationTrustStatementError)
                .bind()

            val canIssue = trustStatement.payloadJson[CLAIM_NAME_CAN_ISSUE]
            val protectedIssuanceAuthorizationObject = canIssue?.let {
                safeJson.safeDecodeElementTo<ProtectedIssuanceAuthorizationObject>(it)
                    .mapError(JsonParsingError::toValidateProtectedIssuanceAuthorizationTrustStatementError)
                    .bind()
            } ?: error("can_issue field missing")

            ProtectedIssuanceAuthorizationTrustStatement(
                typ = typ,
                alg = SignatureAlgorithm.fromStdNameOrThrow(trustStatement.algorithm),
                kid = trustStatementStatusResult.kid,
                profileVersion = trustStatement.signedJwt.header.getCustomParam(CLAIM_NAME_PROFILE_VERSION).toString(),
                jti = trustStatement.signedJwt.jwtClaimsSet.getStringClaim(CLAIM_NAME_JTI),
                iat = trustStatement.issuedAt!!.epochSecond,
                exp = trustStatement.expInstant!!.epochSecond,
                sub = sub,
                status = trustStatementStatusResult.status,
                protectedIssuanceAuthorizationObject = protectedIssuanceAuthorizationObject,
            )
        }.mapError(Throwable::toUnexpected).bind()
    }
}
