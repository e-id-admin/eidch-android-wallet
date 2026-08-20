package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchTrustForIssuanceError
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchTrustForIssuanceError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityV1TrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessProtectedIssuanceAuthorizationTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.CheckActorCompliance
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessProtectedIssuanceAuthorizationTrustStatement
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class FetchTrustForIssuanceImpl @Inject constructor(
    private val processProtectedIssuanceAuthorizationTrustStatement: ProcessProtectedIssuanceAuthorizationTrustStatement,
    private val getActorEnvironment: GetActorEnvironment,
    private val processIdentityV1TrustStatement: ProcessIdentityV1TrustStatement,
    private val checkActorCompliance: CheckActorCompliance,
) : FetchTrustForIssuance {
    override suspend operator fun invoke(
        identityTrustStatement: IdentityV2TrustStatement?,
        protectedIssuanceAuthorizationTrustStatement: Jwt?,
        vcSchemaId: String,
        issuerDid: String,
    ): Result<TrustCheckResult?, FetchTrustForIssuanceError> = coroutineBinding {
        when (getActorEnvironment(issuerDid)) {
            ActorEnvironment.PRODUCTION, ActorEnvironment.BETA ->
                getTrustStatus(
                    identityV2TrustStatement = identityTrustStatement,
                    protectedIssuanceAuthorizationTrustStatementJwt = protectedIssuanceAuthorizationTrustStatement,
                    vct = vcSchemaId,
                    issuerDid = issuerDid,
                ).bind()

            ActorEnvironment.EXTERNAL -> null
        }
    }

    private suspend fun getTrustStatus(
        identityV2TrustStatement: IdentityV2TrustStatement?,
        protectedIssuanceAuthorizationTrustStatementJwt: Jwt?,
        issuerDid: String,
        vct: String,
    ): Result<TrustCheckResult, FetchTrustForIssuanceError> = coroutineBinding {
        val identityTrustStatement = identityV2TrustStatement ?: processIdentityV1TrustStatement(issuerDid)
            .mapError(ProcessIdentityV1TrustStatementError::toFetchTrustForIssuanceError)
            .bind()
        val issuanceTrustStatus = processProtectedIssuanceAuthorizationTrustStatement(
            protectedIssuanceAuthorizationTrustStatement = protectedIssuanceAuthorizationTrustStatementJwt,
            actorDid = issuerDid,
            vct = vct,
        ).mapError(ProcessProtectedIssuanceAuthorizationTrustStatementError::toFetchTrustForIssuanceError)
            .bind()

        val nonComplianceData = checkActorCompliance(actorDid = issuerDid)

        TrustCheckResult(
            identityTrustStatement = identityTrustStatement,
            vcSchemaTrustStatus = issuanceTrustStatus,
            nonComplianceData = nonComplianceData,
        )
    }
}
