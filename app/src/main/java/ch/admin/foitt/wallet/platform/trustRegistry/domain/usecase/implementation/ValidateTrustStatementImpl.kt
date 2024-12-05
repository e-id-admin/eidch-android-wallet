package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.CredentialValidity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.utils.validity
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toValidateTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.utils.BuildConfigProvider
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

class ValidateTrustStatementImpl @Inject constructor(
    private val buildConfigProvider: BuildConfigProvider,
    private val verifyJwtSignature: VerifyJwtSignature,
) : ValidateTrustStatement {
    override suspend operator fun invoke(
        trustStatementSdJwt: String
    ): Result<TrustStatement, ValidateTrustStatementError> = coroutineBinding {
        runSuspendCatching {
            val trustStatement = SdJwt(trustStatementSdJwt)
            val signedTrustStatement = trustStatement.signedJWT

            check(trustStatement.hasTrustedDid()) {
                errorMessageStart + "issuer did is not trusted"
            }

            // Header checks
            check(signedTrustStatement.hasSupportedType()) {
                errorMessageStart + "type is unsupported"
            }
            check(signedTrustStatement.hasSupportedAlgorithm()) {
                errorMessageStart + "algorithm is unsupported"
            }

            verifyJwtSignature(
                issuerDid = trustStatement.issuer,
                signedJwt = signedTrustStatement,
            )
                .mapError(VerifyJwtError::toValidateTrustStatementError)
                .bind()

            // Claim checks
            trustStatement.checkClaimNotNull(JWTClaimNames.ISSUED_AT)

            trustStatement.checkClaimNotNull(JWTClaimNames.NOT_BEFORE)

            trustStatement.checkClaimNotNull(JWTClaimNames.EXPIRATION_TIME)

            check(signedTrustStatement.validity == CredentialValidity.VALID) {
                "$errorMessageStart is ${signedTrustStatement.validity}"
            }

            trustStatement.checkClaimValue(VC_TYPE_KEY, VC_TYPE_VALUE)

            trustStatement.checkClaimNotNull(JWTClaimNames.SUBJECT)

            // Specific trust statements claims
            trustStatement.checkClaimNotNull(VC_ORGNAME_KEY)

            trustStatement.checkClaimNotNull(VC_LOGOURI_KEY)

            trustStatement.checkClaimNotNull(VC_PREFLANG_KEY)

            TrustStatement(sdJwt = trustStatement)
        }
            .mapError(Throwable::toValidateTrustStatementError)
            .bind()
    }

    private fun SdJwt.getRawClaimValue(claimKey: String): JsonElement? = json.jsonObject[claimKey]

    private fun SdJwt.hasTrustedDid() = buildConfigProvider.trustedDids.contains(issuer)

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
