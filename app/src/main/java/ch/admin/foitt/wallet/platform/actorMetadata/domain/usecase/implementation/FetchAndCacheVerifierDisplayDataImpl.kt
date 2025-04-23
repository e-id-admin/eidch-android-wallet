package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheVerifierDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.InitializeActorForScope
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchTrustStatementFromDid
import com.github.michaelbull.result.get
import timber.log.Timber
import javax.inject.Inject

internal class FetchAndCacheVerifierDisplayDataImpl @Inject constructor(
    private val fetchTrustStatementFromDid: FetchTrustStatementFromDid,
    private val initializeActorForScope: InitializeActorForScope,
) : FetchAndCacheVerifierDisplayData {
    override suspend fun invoke(
        presentationRequest: PresentationRequest,
        shouldFetchTrustStatement: Boolean,
    ) {
        val verifierNameDisplay = presentationRequest.clientMetaData?.toVerifierName()
        val verifierLogoDisplay = presentationRequest.clientMetaData?.toVerifierLogo()

        val trustStatement: TrustStatement? = if (shouldFetchTrustStatement) {
            fetchTrustStatementFromDid(
                did = presentationRequest.clientId,
            ).get()
        } else {
            null
        }
        Timber.d("${trustStatement ?: "trust statement not evaluated or failed"}")

        val trustStatementStatus = if (trustStatement != null) {
            TrustStatus.TRUSTED
        } else {
            TrustStatus.NOT_TRUSTED
        }

        val verifierTrustNameDisplay: List<ActorField<String>>? = trustStatement?.orgName?.toActorField() ?: verifierNameDisplay
        val verifierTrustLogoDisplay: List<ActorField<String>>? = verifierLogoDisplay

        val presentationVerifierDisplay = ActorDisplayData(
            name = verifierTrustNameDisplay,
            image = verifierTrustLogoDisplay,
            trustStatus = trustStatementStatus,
            preferredLanguage = trustStatement?.prefLang,
            actorType = ActorType.VERIFIER,
        )

        initializeActorForScope(
            actorDisplayData = presentationVerifierDisplay,
            componentScope = ComponentScope.Verifier,
        )
    }

    private fun <T> Map<String, T>.toActorField(): List<ActorField<T>> = map { entry ->
        ActorField(
            value = entry.value,
            locale = entry.key,
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
}
