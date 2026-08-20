package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.FetchVcSchemaTrustStatusError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessProtectedIssuanceAuthorizationTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementActor
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateProtectedIssuanceAuthorizationTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toProcessProtectedIssuanceAuthorizationTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchVcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessProtectedIssuanceAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessProtectedIssuanceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateProtectedIssuanceAuthorizationTrustStatement
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.getOr
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import timber.log.Timber
import javax.inject.Inject

class ProcessProtectedIssuanceAuthorizationTrustStatementImpl @Inject constructor(
    private val processProtectedIssuanceTrustListStatement: ProcessProtectedIssuanceTrustListStatement,
    private val validateProtectedIssuanceAuthorizationTrustStatement: ValidateProtectedIssuanceAuthorizationTrustStatement,
    private val fetchVcSchemaTrustStatus: FetchVcSchemaTrustStatus,
) : ProcessProtectedIssuanceAuthorizationTrustStatement {
    override suspend fun invoke(
        protectedIssuanceAuthorizationTrustStatement: Jwt?,
        actorDid: String,
        vct: String,
    ): Result<VcSchemaTrustStatus, ProcessProtectedIssuanceAuthorizationTrustStatementError> = coroutineBinding {
        val protectedIssuance = processProtectedIssuanceTrustListStatement(
            issuerDid = actorDid,
            vct = vct,
        ).getOr(true)

        if (protectedIssuance) {
            checkAuthorizedIssuance(
                protectedIssuanceAuthorizationTrustStatement = protectedIssuanceAuthorizationTrustStatement,
                actorDid = actorDid,
                vct = vct,
            ).bind()
            VcSchemaTrustStatus.TRUSTED
        } else {
            VcSchemaTrustStatus.UNPROTECTED
        }
    }.onErr { error ->
        when (error) {
            is TrustRegistryError.UnknownRegistry -> {} // logged at root
            is TrustRegistryError.Unexpected -> Timber.e(t = error.cause, message = "Process piaTS: unauthorized issuance")
        }
    }

    private suspend fun checkAuthorizedIssuance(
        protectedIssuanceAuthorizationTrustStatement: Jwt?,
        actorDid: String,
        vct: String,
    ): Result<Unit, ProcessProtectedIssuanceAuthorizationTrustStatementError> = coroutineBinding {
        if (protectedIssuanceAuthorizationTrustStatement != null) {
            val piaTS = validateProtectedIssuanceAuthorizationTrustStatement(
                trustStatement = protectedIssuanceAuthorizationTrustStatement,
                actorDid = actorDid,
            ).mapError(
                ValidateProtectedIssuanceAuthorizationTrustStatementError::toProcessProtectedIssuanceAuthorizationTrustStatementError
            ).bind()

            if (piaTS.protectedIssuanceAuthorizationObject.vct != vct) {
                val exception = IllegalStateException("not authorized to issue this vct")
                return@coroutineBinding Err(TrustRegistryError.Unexpected(exception)).bind<Unit>()
            }
        } else {
            val trustStatus = fetchVcSchemaTrustStatus(
                trustStatementActor = TrustStatementActor.ISSUER,
                actorDid = actorDid,
                vcSchemaId = vct,
            ).mapError(FetchVcSchemaTrustStatusError::toProcessProtectedIssuanceAuthorizationTrustStatementError)
                .bind()
            if (trustStatus != VcSchemaTrustStatus.TRUSTED) {
                val exception = IllegalStateException("not authorized to issue this vct")
                return@coroutineBinding Err(TrustRegistryError.Unexpected(exception)).bind<Unit>()
            }
        }
    }
}
