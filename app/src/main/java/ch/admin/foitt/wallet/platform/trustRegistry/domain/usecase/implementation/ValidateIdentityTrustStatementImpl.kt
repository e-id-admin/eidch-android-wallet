package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement.Companion.CLAIM_NAME_ENTITY_NAME
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement.Companion.TYPE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IsTrustedDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.RegistryId
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_JTI
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_PROFILE_VERSION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_REGISTRY_ID
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_STATE_ACTOR
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateIdentityTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementJwtError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementStatusError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toUnexpected
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toValidateIdentityTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateIdentityTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementStatus
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOr
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class ValidateIdentityTrustStatementImpl @Inject constructor(
    private val validateTrustStatementJwt: ValidateTrustStatementJwt,
    private val validateTrustStatementStatus: ValidateTrustStatementStatus,
    private val isTrustedDid: IsTrustedDid,
    private val safeJson: SafeJson,
) : ValidateIdentityTrustStatement {
    override suspend fun invoke(
        trustStatement: Jwt,
        actorDid: String,
    ): Result<IdentityV2TrustStatement, ValidateIdentityTrustStatementError> = coroutineBinding {
        validateTrustStatementJwt(
            trustStatement = trustStatement,
        ).mapError(ValidateTrustStatementJwtError::toValidateIdentityTrustStatementError)
            .bind()

        runSuspendCatching {
            val typ = checkNotNull(trustStatement.type) { "type must not be null" }
            check(typ == TYPE) { "type is not identity trust statement" }

            val sub = checkNotNull(trustStatement.subject) { "sub must not be null" }
            check(sub == actorDid)

            val trustStatementStatusResult = validateTrustStatementStatus(
                trustStatement = trustStatement,
            ).mapError(ValidateTrustStatementStatusError::toValidateIdentityTrustStatementError)
                .bind()

            isTrustedDid(
                keyId = trustStatementStatusResult.kid,
                trustStatementType = TYPE,
            ).mapError(IsTrustedDidError::toValidateIdentityTrustStatementError)
                .bind()

            val entityName = trustStatement.payloadJson.mapNotNull { (key, value) ->
                if (key.startsWith(CLAIM_NAME_ENTITY_NAME)) {
                    val locale = key.split("#").getOrNull(1) ?: DisplayLanguage.FALLBACK
                    val name = value.jsonPrimitive.content
                    Pair(locale, name)
                } else {
                    null
                }
            }.toMap()

            val isStateActor = trustStatement.signedJwt.jwtClaimsSet.getBooleanClaim(CLAIM_NAME_STATE_ACTOR)

            val registryIdJson = trustStatement.payloadJson[CLAIM_NAME_REGISTRY_ID]
            val registryIds = registryIdJson?.let {
                safeJson.safeDecodeElementTo<List<RegistryId>>(it).getOr(emptyList())
            } ?: emptyList()

            IdentityV2TrustStatement(
                typ = typ,
                alg = SignatureAlgorithm.fromStdNameOrThrow(trustStatement.algorithm),
                kid = trustStatementStatusResult.kid,
                profileVersion = trustStatement.signedJwt.header.getCustomParam(CLAIM_NAME_PROFILE_VERSION).toString(),
                jti = trustStatement.signedJwt.jwtClaimsSet.getStringClaim(CLAIM_NAME_JTI),
                iat = trustStatement.issuedAt!!.epochSecond,
                exp = trustStatement.expInstant!!.epochSecond,
                sub = sub,
                status = trustStatementStatusResult.status,
                entityName = entityName,
                isStateActor = isStateActor,
                registryIds = registryIds,
            )
        }.mapError(Throwable::toUnexpected).bind()
    }
}
