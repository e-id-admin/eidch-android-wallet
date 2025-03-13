package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchIssuerDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialIssuerDisplayRepo
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchTrustStatementFromDid
import com.github.michaelbull.result.get
import javax.inject.Inject

class FetchIssuerDisplayDataImpl @Inject constructor(
    private val credentialIssuerDisplayRepo: CredentialIssuerDisplayRepo,
    private val fetchTrustStatementFromDid: FetchTrustStatementFromDid,
    private val getAnyCredential: GetAnyCredential,
) : FetchIssuerDisplayData {
    override suspend fun invoke(credentialId: Long): ActorDisplayData {
        val credential: AnyCredential? = getAnyCredential(credentialId).get()
        val credentialIssuerDisplays = credentialIssuerDisplayRepo.getIssuerDisplays(credentialId).get()

        val trustStatement = getCredentialIssuerDid(credential?.issuer)?.let { issuerDid ->
            fetchTrustStatementFromDid(
                did = issuerDid,
            ).get()
        }

        val trustStatementStatus = if (trustStatement != null) {
            TrustStatus.TRUSTED
        } else {
            TrustStatus.NOT_TRUSTED
        }

        val issuerTrustNameDisplay: List<ActorField<String>>? =
            trustStatement?.orgName?.toActorField() ?: credentialIssuerDisplays?.toIssuerName()
        val issuerTrustLogoDisplay: List<ActorField<String>>? =
            trustStatement?.logoUri?.toActorField() ?: credentialIssuerDisplays?.toIssuerLogo()

        val offerIssuerDisplay = ActorDisplayData(
            name = issuerTrustNameDisplay,
            image = issuerTrustLogoDisplay,
            trustStatus = trustStatementStatus,
            preferredLanguage = trustStatement?.prefLang,
            actorType = ActorType.ISSUER,
        )

        return offerIssuerDisplay
    }

    private fun <T> Map<String, T>.toActorField(): List<ActorField<T>> = map { entry ->
        ActorField(
            value = entry.value,
            locale = entry.key,
        )
    }

    private fun List<CredentialIssuerDisplay>.toIssuerName(): List<ActorField<String>> = map { entry ->
        ActorField(
            value = entry.name,
            locale = entry.locale,
        )
    }

    private fun List<CredentialIssuerDisplay>.toIssuerLogo(): List<ActorField<String>> = mapNotNull { entry ->
        entry.image?.let {
            ActorField(
                value = entry.image,
                locale = entry.locale,
            )
        }
    }

    private fun getCredentialIssuerDid(issuer: String?): String? {
        return if (issuer == null || !issuer.startsWith("did")) {
            null
        } else {
            issuer
        }
    }
}
