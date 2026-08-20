package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.FetchTrustForVerificationError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.toFetchTrustForVerificationError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchTrustForVerification
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.CheckActorCompliance
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityV1TrustStatement
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class FetchTrustForVerificationImpl @Inject constructor(
    private val getActorEnvironment: GetActorEnvironment,
    private val processIdentityV1TrustStatement: ProcessIdentityV1TrustStatement,
    private val processIdentityTrustStatement: ProcessIdentityTrustStatement,
    private val checkActorCompliance: CheckActorCompliance,
) : FetchTrustForVerification {
    override suspend fun invoke(
        authorizationRequest: AuthorizationRequest,
    ): Result<TrustCheckResult, FetchTrustForVerificationError> = coroutineBinding {
        val identityV2Jwt = authorizationRequest.verifierInfo?.firstOrNull { verifierInfo ->
            verifierInfo.data.type == IdentityV2TrustStatement.TYPE
        }?.data

        val verifierDid = authorizationRequest.clientIdentifier.clientId

        val identityV2TrustStatement = identityV2Jwt?.let { jwt ->
            processIdentityTrustStatement(
                identityTrustStatementJwt = jwt,
                actorDid = verifierDid,
            ).mapError { error ->
                error.toFetchTrustForVerificationError(authorizationRequest.responseUri)
            }.bind()
        }

        val environment = getActorEnvironment(verifierDid)
        val identityTrustStatement = identityV2TrustStatement ?: getIdentityV1TrustStatement(
            environment = environment,
            verifierDid = verifierDid,
            responseUri = authorizationRequest.responseUri
        ).bind()
        val nonComplianceData = checkActorCompliance(actorDid = verifierDid)

        TrustCheckResult(
            identityTrustStatement = identityTrustStatement,
            vcSchemaTrustStatus = VcSchemaTrustStatus.UNPROTECTED,
            nonComplianceData = nonComplianceData,
        )
    }

    private suspend fun getIdentityV1TrustStatement(
        environment: ActorEnvironment,
        verifierDid: String,
        responseUri: String?,
    ): Result<IdentityTrustStatement?, FetchTrustForVerificationError> = coroutineBinding {
        when (environment) {
            ActorEnvironment.PRODUCTION, ActorEnvironment.BETA -> {
                processIdentityV1TrustStatement(verifierDid)
                    .mapError { error ->
                        error.toFetchTrustForVerificationError(responseUri)
                    }.bind()
            }

            ActorEnvironment.EXTERNAL -> null
        }
    }
}
