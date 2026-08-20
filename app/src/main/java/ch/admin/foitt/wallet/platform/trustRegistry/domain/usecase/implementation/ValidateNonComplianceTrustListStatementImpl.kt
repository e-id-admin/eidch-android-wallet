package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IsTrustedDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceTrustListStatement.Companion.CLAIM_NAME_ACTOR
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceTrustListStatement.Companion.CLAIM_NAME_NON_COMPLIANT_ACTORS
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceTrustListStatement.Companion.CLAIM_NAME_REASON
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceTrustListStatement.Companion.TYPE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_JTI
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_PROFILE_VERSION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateNonComplianceTrustListStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementJwtError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementStatusError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toUnexpected
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toValidateNonComplianceTrustListStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateNonComplianceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementStatus
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class ValidateNonComplianceTrustListStatementImpl @Inject constructor(
    private val validateTrustStatementJwt: ValidateTrustStatementJwt,
    private val validateTrustStatementStatus: ValidateTrustStatementStatus,
    private val isTrustedDid: IsTrustedDid,
) : ValidateNonComplianceTrustListStatement {
    override suspend fun invoke(
        trustStatement: Jwt,
        actorDid: String,
    ): Result<NonComplianceTrustListStatement, ValidateNonComplianceTrustListStatementError> = coroutineBinding {
        validateTrustStatementJwt(
            trustStatement = trustStatement,
        ).mapError(ValidateTrustStatementJwtError::toValidateNonComplianceTrustListStatementError)
            .bind()

        runSuspendCatching {
            val typ = checkNotNull(trustStatement.type) { "type must not be null" }
            check(typ == TYPE) { "type is not non-compliance trust list statement" }

            val trustStatementStatusResult = validateTrustStatementStatus(
                trustStatement = trustStatement,
            ).mapError(ValidateTrustStatementStatusError::toValidateNonComplianceTrustListStatementError)
                .bind()

            isTrustedDid(
                keyId = trustStatementStatusResult.kid,
                trustStatementType = TYPE,
            ).mapError(IsTrustedDidError::toValidateNonComplianceTrustListStatementError)
                .bind()

            NonComplianceTrustListStatement(
                typ = typ,
                alg = SignatureAlgorithm.fromStdNameOrThrow(trustStatement.algorithm),
                kid = trustStatementStatusResult.kid,
                profileVersion = trustStatement.signedJwt.header.getCustomParam(CLAIM_NAME_PROFILE_VERSION).toString(),
                jti = checkNotNull(trustStatement.signedJwt.jwtClaimsSet.getStringClaim(CLAIM_NAME_JTI)) {
                    "jti must not be null"
                },
                iat = checkNotNull(trustStatement.issuedAt) { "iat must not be null" }.epochSecond,
                exp = checkNotNull(trustStatement.expInstant) { "exp must not be null" }.epochSecond,
                status = trustStatementStatusResult.status,
                nonCompliantActors = trustStatement.payloadJson.toNonCompliantActors(),
            )
        }.mapError(Throwable::toUnexpected).bind()
    }

    private fun JsonObject.toNonCompliantActors(): List<NonComplianceTrustListStatement.NonCompliantActor> {
        val actors = checkNotNull(this[CLAIM_NAME_NON_COMPLIANT_ACTORS]) {
            "non compliant actors must not be null"
        }.jsonArray

        return actors.map { actorElement ->
            val actorJson = actorElement.jsonObject
            val baseReason = when (val reason = actorJson[CLAIM_NAME_REASON]) {
                null,
                JsonNull -> error("reason must not be null")
                is JsonObject -> error("reason must not be an object")
                else -> mapOf(DisplayLanguage.FALLBACK to reason.jsonPrimitive.content)
            }
            val localizedReasons = actorJson.mapNotNull { (key, value) ->
                if (key.startsWith("$CLAIM_NAME_REASON#")) {
                    val locale = key.split("#").getOrNull(1)
                    locale?.let { it to value.jsonPrimitive.content }
                } else {
                    null
                }
            }.toMap()
            val reason = (baseReason + localizedReasons).takeIf { it.isNotEmpty() }

            NonComplianceTrustListStatement.NonCompliantActor(
                actor = checkNotNull(actorJson[CLAIM_NAME_ACTOR]) { "actor must not be null" }.jsonPrimitive.content,
                reason = reason,
            )
        }
    }
}
