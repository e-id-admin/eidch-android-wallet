package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceReasonDisplay
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.CheckActorCompliance
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateNonComplianceTrustListStatement
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapBoth
import javax.inject.Inject

class CheckActorComplianceImpl @Inject constructor(
    private val getTrustDomainFromDid: GetTrustDomainFromDid,
    private val trustStatementRepository: TrustStatementRepository,
    private val validateNonComplianceTrustListStatement: ValidateNonComplianceTrustListStatement,
) : CheckActorCompliance {
    override suspend fun invoke(actorDid: String): NonComplianceData = coroutineBinding {
        val trustRegistryDomain = getTrustDomainFromDid(actorDid)
            .bind()

        val ncTLSJwt = trustStatementRepository.fetchNonComplianceTrustListStatement(trustRegistryDomain)
            .bind()

        validateNonComplianceTrustListStatement(
            trustStatement = ncTLSJwt,
            actorDid = actorDid,
        ).bind()
    }
        .mapBoth(
            success = { nonComplianceTrustListStatement ->
                // check if provided did is part of reported actors list
                nonComplianceTrustListStatement.nonCompliantActors.find { it.actor == actorDid }?.let { actor ->
                    val displays = actor.reason?.map { (locale, translation) ->
                        NonComplianceReasonDisplay(locale = locale, reason = translation)
                    }

                    NonComplianceData(
                        state = ActorComplianceState.REPORTED,
                        reasonDisplays = displays,
                    )
                } ?: notReported
            },
            failure = {
                unknown
            }
        )

    private val notReported = NonComplianceData(
        state = ActorComplianceState.NOT_REPORTED,
        reasonDisplays = null,
    )

    private val unknown = NonComplianceData(
        state = ActorComplianceState.UNKNOWN,
        reasonDisplays = null,
    )
}
