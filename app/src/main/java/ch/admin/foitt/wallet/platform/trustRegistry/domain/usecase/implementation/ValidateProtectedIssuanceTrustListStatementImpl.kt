package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IsTrustedDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceTrustListStatement.Companion.CLAIM_NAME_VCT_VALUES
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceTrustListStatement.Companion.TYPE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_JTI
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_PROFILE_VERSION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateProtectedIssuanceTrustListStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementJwtError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementStatusError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toUnexpected
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toValidateProtectedIssuanceTrustListStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateProtectedIssuanceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementStatus
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class ValidateProtectedIssuanceTrustListStatementImpl @Inject constructor(
    private val validateTrustStatementJwt: ValidateTrustStatementJwt,
    private val validateTrustStatementStatus: ValidateTrustStatementStatus,
    private val isTrustedDid: IsTrustedDid,
    private val safeJson: SafeJson,
) : ValidateProtectedIssuanceTrustListStatement {
    override suspend fun invoke(
        trustStatement: Jwt
    ): Result<ProtectedIssuanceTrustListStatement, ValidateProtectedIssuanceTrustListStatementError> = coroutineBinding {
        validateTrustStatementJwt(
            trustStatement = trustStatement,
        ).mapError(ValidateTrustStatementJwtError::toValidateProtectedIssuanceTrustListStatementError)
            .bind()

        runSuspendCatching {
            val typ = checkNotNull(trustStatement.type) { "type must not be null" }
            check(typ == TYPE) { "type is not protected issuance trust list statement" }

            val trustStatementStatusResult = validateTrustStatementStatus(
                trustStatement = trustStatement,
            ).mapError(ValidateTrustStatementStatusError::toValidateProtectedIssuanceTrustListStatementError)
                .bind()

            isTrustedDid(
                keyId = trustStatementStatusResult.kid,
                trustStatementType = TYPE,
            ).mapError(IsTrustedDidError::toValidateProtectedIssuanceTrustListStatementError)
                .bind()

            val vctValuesJson = trustStatement.payloadJson[CLAIM_NAME_VCT_VALUES]
            val vctValues = vctValuesJson?.let {
                safeJson.safeDecodeElementTo<List<String>>(vctValuesJson)
                    .mapError(JsonParsingError::toValidateProtectedIssuanceTrustListStatementError)
                    .bind()
            } ?: error("vct values claim is missing")

            ProtectedIssuanceTrustListStatement(
                typ = typ,
                alg = SignatureAlgorithm.fromStdNameOrThrow(trustStatement.algorithm),
                kid = trustStatementStatusResult.kid,
                profileVersion = trustStatement.signedJwt.header.getCustomParam(CLAIM_NAME_PROFILE_VERSION).toString(),
                jti = trustStatement.signedJwt.jwtClaimsSet.getStringClaim(CLAIM_NAME_JTI),
                iat = trustStatement.issuedAt!!.epochSecond,
                exp = trustStatement.expInstant!!.epochSecond,
                status = trustStatementStatusResult.status,
                vctValues = vctValues,
            )
        }.mapError(Throwable::toUnexpected).bind()
    }
}
