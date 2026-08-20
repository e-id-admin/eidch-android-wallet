package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.FetchAndCacheVerifierDisplayDataError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.FetchTrustForVerificationError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.toFetchAndCacheVerifierDisplayDataError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheVerifierDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchTrustForVerification
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.InitializeActorForScope
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.VerificationProcessType
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceReasonDisplay
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import timber.log.Timber
import javax.inject.Inject

internal class FetchAndCacheVerifierDisplayDataImpl @Inject constructor(
    private val getActorEnvironment: GetActorEnvironment,
    private val fetchTrustForVerification: FetchTrustForVerification,
    private val initializeActorForScope: InitializeActorForScope,
) : FetchAndCacheVerifierDisplayData {
    override suspend fun invoke(
        authorizationRequest: AuthorizationRequest,
        verificationProcessType: VerificationProcessType,
        verifierAttestationTrusted: Boolean?,
    ): Result<Unit, FetchAndCacheVerifierDisplayDataError> = coroutineBinding {
        val verifierNameDisplay = authorizationRequest.clientMetaData?.toVerifierName()
        val verifierLogoDisplay = authorizationRequest.clientMetaData?.toVerifierLogo()

        if (verificationProcessType == VerificationProcessType.PROXIMITY) {
            cacheProximityVerifierFromMetadata(
                verifierNameDisplay = verifierNameDisplay,
                verifierLogoDisplay = verifierLogoDisplay,
                verifierAttestationTrusted = verifierAttestationTrusted,
            )
            return@coroutineBinding Unit
        }
        val verifierDid = authorizationRequest.clientIdentifier.clientId
        val environment = getActorEnvironment(verifierDid)
        val trustCheckResult = fetchTrustForVerification(authorizationRequest)
            .mapError(FetchTrustForVerificationError::toFetchAndCacheVerifierDisplayDataError)
            .bind()

        val trustStatement = trustCheckResult.identityTrustStatement

        Timber.d("${trustStatement ?: "trust statement not evaluated or failed"}")

        val trustStatus = getTrustStatus(
            environment = environment,
            trustStatement = trustStatement
        )

        val vcSchemaTrustStatus = trustCheckResult.vcSchemaTrustStatus

        val verifierTrustNameDisplay: List<ActorField<String>>? =
            trustStatement?.entityName?.filterKeys {
                it.isNotBlank()
            }?.toActorField() // trust statement available -> use it without metadata as default
                ?: verifierNameDisplay // trust statement not available -> use metadata
        val verifierTrustLogoDisplay: List<ActorField<String>>? = verifierLogoDisplay

        val nonComplianceData = trustCheckResult.nonComplianceData
        val nonComplianceReason: List<ActorField<String>>? = nonComplianceData.reasonDisplays?.toNonComplianceReason()

        val presentationVerifierDisplay = ActorDisplayData(
            name = verifierTrustNameDisplay,
            image = verifierTrustLogoDisplay,
            trustStatus = trustStatus,
            vcSchemaTrustStatus = vcSchemaTrustStatus,
            preferredLanguage = null,
            actorType = ActorType.VERIFIER,
            actorComplianceState = nonComplianceData.state,
            nonComplianceReason = nonComplianceReason,
        )

        initializeActorForScope(
            actorDisplayData = presentationVerifierDisplay,
            componentScope = ComponentScope.Verifier,
        )
    }

    private suspend fun cacheProximityVerifierFromMetadata(
        verifierNameDisplay: List<ActorField<String>>?,
        verifierLogoDisplay: List<ActorField<String>>?,
        verifierAttestationTrusted: Boolean?,
    ) {
        val trustStatus = if (verifierAttestationTrusted == true) {
            TrustStatus.TRUSTED_PROXIMITY_VERIFIER
        } else {
            TrustStatus.NOT_TRUSTED_PROXIMITY_VERIFIER
        }

        val vcSchemaTrustStatus = if (verifierAttestationTrusted == true) {
            VcSchemaTrustStatus.TRUSTED
        } else {
            VcSchemaTrustStatus.NOT_TRUSTED
        }

        initializeActorForScope(
            actorDisplayData = ActorDisplayData(
                name = verifierNameDisplay,
                image = verifierLogoDisplay,
                trustStatus = trustStatus,
                vcSchemaTrustStatus = vcSchemaTrustStatus,
                preferredLanguage = null,
                actorType = ActorType.VERIFIER,
                actorComplianceState = ActorComplianceState.UNKNOWN,
                nonComplianceReason = null,
            ),
            componentScope = ComponentScope.Verifier,
        )
    }

    private fun ClientMetaData.toVerifierName(): List<ActorField<String>> = clientNameList.map { entry ->
        ActorField(
            value = entry.clientName,
            locale = entry.locale,
        )
    }

    private fun ClientMetaData.toVerifierLogo(): List<ActorField<String>> = logoUriList.map { entry ->
        ActorField(
            value = entry.logoUri,
            locale = entry.locale,
        )
    }

    private fun getTrustStatus(
        environment: ActorEnvironment,
        trustStatement: IdentityTrustStatement?
    ) = when (environment) {
        ActorEnvironment.PRODUCTION, ActorEnvironment.BETA -> {
            if (trustStatement != null) {
                TrustStatus.TRUSTED
            } else {
                TrustStatus.NOT_TRUSTED
            }
        }

        ActorEnvironment.EXTERNAL -> TrustStatus.EXTERNAL
    }

    private fun <T> Map<String, T>.toActorField(): List<ActorField<T>> = map { entry ->
        ActorField(
            value = entry.value,
            locale = entry.key,
        )
    }

    private fun List<NonComplianceReasonDisplay>.toNonComplianceReason(): List<ActorField<String>> = map { entry ->
        ActorField(
            value = entry.reason,
            locale = entry.locale,
        )
    }
}
