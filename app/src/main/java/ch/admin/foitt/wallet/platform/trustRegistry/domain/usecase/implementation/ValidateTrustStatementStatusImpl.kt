package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.FetchCredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementStatusResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementStatusError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toUnexpected
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toValidateTrustStatementStatusError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementStatus
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class ValidateTrustStatementStatusImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val didResolverHelper: DidResolverHelper,
    private val fetchCredentialStatus: FetchCredentialStatus,
) : ValidateTrustStatementStatus {
    override suspend fun invoke(
        trustStatement: Jwt,
    ): Result<TrustStatementStatusResult, ValidateTrustStatementStatusError> = coroutineBinding {
        runSuspendCatching {
            val statusJson = checkNotNull(trustStatement.payloadJson[TrustStatement.CLAIM_NAME_STATUS]) {
                "status must not be null"
            }
            val status = checkNotNull(
                safeJson.safeDecodeElementTo<CredentialStatusProperties>(statusJson).get()
            ) { "invalid status property" }
            val kid = checkNotNull(trustStatement.keyId)
            val issuerDid = didResolverHelper.getDidStringFromAbsoluteKeyId(kid)
                .mapError(Throwable::toUnexpected).bind()
            val statusResult = fetchCredentialStatus(
                credentialIssuer = issuerDid,
                properties = status,
            ).mapError(FetchCredentialStatusError::toValidateTrustStatementStatusError)
                .bind()
            check(statusResult == CredentialStatus.VALID)

            TrustStatementStatusResult(
                status = status,
                kid = kid,
            )
        }.mapError(Throwable::toUnexpected).bind()
    }
}
