package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.shouldFetchTrustStatements
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchVerifierDisplayData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchTrustStatementFromDid
import com.github.michaelbull.result.get
import timber.log.Timber
import javax.inject.Inject

class FetchVerifierDisplayDataImpl @Inject constructor(
    private val fetchTrustStatementFromDid: FetchTrustStatementFromDid,
) : FetchVerifierDisplayData {
    override suspend fun invoke(presentationRequest: PresentationRequest): ActorDisplayData {
        val verifierNameDisplay = presentationRequest.clientMetaData?.toVerifierName()
        val verifierLogoDisplay = presentationRequest.clientMetaData?.toVerifierLogo()

        val trustStatement: TrustStatement? = if (presentationRequest.shouldFetchTrustStatements()) {
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
        val verifierTrustLogoDisplay: List<ActorField<String>>? = trustStatement?.logoUri?.toActorField() ?: verifierLogoDisplay

        val presentationVerifierDisplay = ActorDisplayData(
            name = verifierTrustNameDisplay,
            image = verifierTrustLogoDisplay,
            trustStatus = trustStatementStatus,
            preferredLanguage = trustStatement?.prefLang,
        )

        return presentationVerifierDisplay
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

    private fun ClientMetaData.toVerifierLogo(): List<ActorField<String>>? = logoUriList.map { entry ->
        ActorField(
            value = entry.logoUri,
            locale = entry.locale,
        )
    }
}
