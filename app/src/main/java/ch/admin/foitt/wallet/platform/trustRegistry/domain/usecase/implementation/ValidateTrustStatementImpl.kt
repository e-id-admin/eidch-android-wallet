package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.CredentialValidity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
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
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject

internal class ValidateTrustStatementImpl @Inject constructor(
    private val environmentSetupRepo: EnvironmentSetupRepository,
    private val verifyJwtSignature: VerifyJwtSignature,
    private val fetchCredentialStatus: FetchCredentialStatus,
    private val safeJson: SafeJson,
) : ValidateTrustStatement {
    override suspend operator fun invoke(
        trustStatementRawVcSdJwt: String
    ): Result<TrustStatement, ValidateTrustStatementError> = coroutineBinding {
        runSuspendCatching {
            val trustStatement = VcSdJwt(trustStatementRawVcSdJwt)

            check(trustStatement.hasTrustedDid()) {
                errorMessageStart + "issuer did is not trusted"
            }

            // Header checks
            check(trustStatement.type == SDJWT_TYPE_VALUE) {
                errorMessageStart + "type is unsupported"
            }
            check(trustStatement.algorithm == SigningAlgorithm.ES256.stdName) {
                errorMessageStart + "algorithm is unsupported"
            }

            verifyJwtSignature(
                did = trustStatement.vcIssuer,
                kid = trustStatement.kid,
                jwt = trustStatement,
            )
                .mapError(VerifyJwtError::toValidateTrustStatementError)
                .bind()

            // Claim checks
            checkNotNull(trustStatement.issuedAt) { "$errorMessageStart iat is missing" }
            checkNotNull(trustStatement.notBefore) { "$errorMessageStart nbf is missing" }
            checkNotNull(trustStatement.expiredAt) { "$errorMessageStart exp is missing" }
            checkNotNull(trustStatement.subject) { "$errorMessageStart sub is missing" }
            check(trustStatement.jwtValidity == CredentialValidity.Valid) {
                "$errorMessageStart is ${trustStatement.jwtValidity}"
            }
            check(trustStatement.vct == VC_TYPE_VALUE) { "$errorMessageStart vct is missing" }

            // Specific trust statements claims
            trustStatement.checkClaimNotNull(VC_ORGNAME_KEY)

            trustStatement.checkClaimNotNull(VC_PREFLANG_KEY)

            // Status of trust statement
            val statusJsonElement = checkNotNull(trustStatement.status)
            val statusProperties =
                checkNotNull(safeJson.safeDecodeElementTo<CredentialStatusProperties>(statusJsonElement).get()) {
                    "$errorMessageStart has no status"
                }

            val trustStatementStatus = fetchCredentialStatus(trustStatement.vcIssuer, statusProperties).get()

            check(trustStatementStatus == CredentialStatus.VALID) {
                "$errorMessageStart status is not valid"
            }

            val trustStatementObject = safeJson.safeDecodeElementTo<TrustStatement>(trustStatement.sdJwtJson)
                .mapError(JsonParsingError::toValidateTrustStatementError)
                .bind()
            trustStatementObject
        }
            .mapError { throwable ->
                throwable.toValidateTrustStatementError("ValidateTrustStatement error")
            }.bind()
    }

    private fun VcSdJwt.hasTrustedDid() = environmentSetupRepo.trustedDids.contains(vcIssuer)

    private fun SdJwt.checkClaimNotNull(claimKey: String): JsonElement = checkNotNull(sdJwtJson.jsonObject[claimKey]) {
        "$errorMessageStart $claimKey is missing"
    }

    private val errorMessageStart = "Trust statement "

    private companion object {
        const val VC_TYPE_VALUE = "TrustStatementMetadataV1"
        const val SDJWT_TYPE_VALUE = "vc+sd-jwt"
        const val VC_ORGNAME_KEY = "orgName"
        const val VC_PREFLANG_KEY = "prefLang"
    }
}
