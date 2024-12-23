package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.CredentialValidity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.utils.validity
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toValidateTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.nimbusds.jwt.JWTClaimNames
import com.nimbusds.jwt.SignedJWT
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

internal class ValidateTrustStatementImpl @Inject constructor(
    private val environmentSetupRepo: EnvironmentSetupRepository,
    private val verifyJwtSignature: VerifyJwtSignature,
    private val safeJson: SafeJson,
) : ValidateTrustStatement {
    override suspend operator fun invoke(
        trustStatementRawSdJwt: String
    ): Result<TrustStatement, ValidateTrustStatementError> = coroutineBinding {
        runSuspendCatching {
            val trustStatementSdJwt = SdJwt(trustStatementRawSdJwt)
            val trustStatementSignedJwt = trustStatementSdJwt.signedJWT

            check(trustStatementSdJwt.hasTrustedDid()) {
                errorMessageStart + "issuer did is not trusted"
            }

            // Header checks
            check(trustStatementSignedJwt.hasSupportedType()) {
                errorMessageStart + "type is unsupported"
            }
            check(trustStatementSignedJwt.hasSupportedAlgorithm()) {
                errorMessageStart + "algorithm is unsupported"
            }

            verifyJwtSignature(
                did = trustStatementSignedJwt.jwtClaimsSet.issuer,
                kid = trustStatementSignedJwt.header.keyID,
                signedJwt = trustStatementSignedJwt,
            )
                .mapError(VerifyJwtError::toValidateTrustStatementError)
                .bind()

            // Claim checks
            trustStatementSdJwt.checkClaimNotNull(JWTClaimNames.ISSUED_AT)

            trustStatementSdJwt.checkClaimNotNull(JWTClaimNames.NOT_BEFORE)

            trustStatementSdJwt.checkClaimNotNull(JWTClaimNames.EXPIRATION_TIME)

            check(trustStatementSignedJwt.validity == CredentialValidity.VALID) {
                "$errorMessageStart is ${trustStatementSignedJwt.validity}"
            }

            trustStatementSdJwt.checkClaimValue(VC_TYPE_KEY, VC_TYPE_VALUE)

            trustStatementSdJwt.checkClaimNotNull(JWTClaimNames.SUBJECT)

            // Specific trust statements claims
            trustStatementSdJwt.checkClaimNotNull(VC_ORGNAME_KEY)

            trustStatementSdJwt.checkClaimNotNull(VC_LOGOURI_KEY)

            trustStatementSdJwt.checkClaimNotNull(VC_PREFLANG_KEY)

            val trustStatementObject = safeJson.safeDecodeElementTo<TrustStatement>(trustStatementSdJwt.json)
                .mapError(JsonParsingError::toValidateTrustStatementError)
                .bind()
            trustStatementObject
        }
            .mapError(Throwable::toValidateTrustStatementError)
            .bind()
    }

    private fun SdJwt.getRawClaimValue(claimKey: String): JsonElement? = json.jsonObject[claimKey]

    private fun SdJwt.hasTrustedDid() = environmentSetupRepo.trustedDids.contains(issuer)

    private fun SignedJWT.hasSupportedType() = header.type.type == SDJWT_TYPE_VALUE

    private fun SignedJWT.hasSupportedAlgorithm() = header.algorithm.name == SigningAlgorithm.ES256.stdName

    private fun SdJwt.checkClaimNotNull(claimKey: String): JsonElement = checkNotNull(getRawClaimValue(claimKey)) {
        "$errorMessageStart $claimKey is missing"
    }

    private fun SdJwt.checkClaimValue(claimKey: String, expectedValue: String) {
        val actualValue = checkClaimNotNull(claimKey)
        check(actualValue.jsonPrimitive.content == expectedValue) {
            "$errorMessageStart $claimKey field value $actualValue is unsupported"
        }
    }

    private val errorMessageStart = "Trust statement"

    private companion object {
        const val VC_TYPE_KEY = "vct"
        const val VC_TYPE_VALUE = "TrustStatementMetadataV1"
        const val SDJWT_TYPE_VALUE = "vc+sd-jwt"
        const val VC_ORGNAME_KEY = "orgName"
        const val VC_PREFLANG_KEY = "prefLang"
        const val VC_LOGOURI_KEY = "logoUri"
    }
}
