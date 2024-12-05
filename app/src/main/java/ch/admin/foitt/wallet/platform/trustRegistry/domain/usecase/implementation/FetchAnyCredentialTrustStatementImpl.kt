package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.FetchAnyCredentialTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.FetchTrustStatementFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toFetchAnyCredentialTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchAnyCredentialTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchTrustStatementFromDid
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class FetchAnyCredentialTrustStatementImpl @Inject constructor(
    private val fetchTrustStatementFromDid: FetchTrustStatementFromDid,
) : FetchAnyCredentialTrustStatement {
    override suspend operator fun invoke(
        anyCredential: AnyCredential
    ): Result<TrustStatement, FetchAnyCredentialTrustStatementError> = coroutineBinding {
        runSuspendCatching {
            when (anyCredential.format) {
                CredentialFormat.VC_SD_JWT -> {
                    val sdJwt = SdJwt(anyCredential.payload)
                    val issuerDid = sdJwt.issuer
                    val trustStatement = fetchTrustStatementFromDid(issuerDid)
                        .mapError(FetchTrustStatementFromDidError::toFetchAnyCredentialTrustStatementError)
                        .bind()

                    trustStatement
                }

                CredentialFormat.UNKNOWN -> error("Unsupported credential format")
            }
        }
            .mapError(Throwable::toFetchAnyCredentialTrustStatementError)
            .bind()
    }
}
