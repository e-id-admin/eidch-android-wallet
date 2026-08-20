package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidateVerificationQueryPublicStatement
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IsTrustedDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementJwtError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateVqPsError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement.Companion.CLAIM_NAME_JTI
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement.Companion.CLAIM_NAME_PROFILE_VERSION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement.Companion.CLAIM_NAME_PURPOSE_DESCRIPTION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement.Companion.CLAIM_NAME_PURPOSE_NAME
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement.Companion.CLAIM_NAME_REQUEST
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement.Companion.TYPE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VqPsRequest
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VqPsRequest.Companion.TYPE_DCQL
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toUnexpected
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toValidateVqPsError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class ValidateVerificationQueryPublicStatementImpl @Inject constructor(
    private val validateTrustStatementJwt: ValidateTrustStatementJwt,
    private val isTrustedDid: IsTrustedDid,
    private val safeJson: SafeJson,
) : ValidateVerificationQueryPublicStatement {
    override suspend fun invoke(
        jwt: Jwt,
        actorDid: String,
    ): Result<VerificationQueryPublicStatement, ValidateVqPsError> = coroutineBinding {
        validateTrustStatementJwt(jwt)
            .mapError(ValidateTrustStatementJwtError::toValidateVqPsError)
            .bind()

        runSuspendCatching {
            val typ = checkNotNull(jwt.type) { "type must not be null" }
            check(typ == TYPE) { "type is not verification query statement" }

            val sub = checkNotNull(jwt.subject) { "sub must not be null" }
            check(sub == actorDid)

            val keyId = checkNotNull(jwt.keyId) { "keyId must not be null" }
            isTrustedDid(
                keyId = keyId,
                trustStatementType = TYPE,
            ).mapError(IsTrustedDidError::toValidateVqPsError)
                .bind()

            val purposeNames = mapClaims(jwt, CLAIM_NAME_PURPOSE_NAME)
            val purposeDescriptions = mapClaims(jwt, CLAIM_NAME_PURPOSE_DESCRIPTION)

            val requestJson = checkNotNull(jwt.payloadJson[CLAIM_NAME_REQUEST]) { "request claim is missing" }

            val vqPsRequest = safeJson.safeDecodeElementTo<VqPsRequest>(requestJson)
                .mapError(JsonParsingError::toValidateVqPsError)
                .bind()

            check(vqPsRequest.type == TYPE_DCQL) { "request type must be DCQL" }

            VerificationQueryPublicStatement(
                typ = typ,
                alg = SignatureAlgorithm.fromStdNameOrThrow(jwt.algorithm),
                kid = keyId,
                profileVersion = jwt.signedJwt.header.getCustomParam(CLAIM_NAME_PROFILE_VERSION).toString(),
                jti = jwt.signedJwt.jwtClaimsSet.getStringClaim(CLAIM_NAME_JTI),
                iat = jwt.issuedAt!!.epochSecond,
                exp = jwt.expInstant!!.epochSecond,
                sub = sub,
                purposeName = purposeNames,
                purposeDescription = purposeDescriptions,
                request = vqPsRequest
            )
        }.mapError { throwable ->
            throwable.toUnexpected(throwable.message ?: "")
        }.bind()
    }

    private fun mapClaims(jwt: Jwt, claimName: String): Map<String, String> =
        jwt.payloadJson.mapNotNull { (key, value) ->
            if (key == claimName || key.startsWith("$claimName#")) {
                val locale = key.split("#").getOrNull(1) ?: DisplayLanguage.FALLBACK
                Pair(locale, value.jsonPrimitive.content)
            } else {
                null
            }
        }.toMap()
}
