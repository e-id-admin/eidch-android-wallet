package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Claim
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Display
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.SaveCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toSaveCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.LocalizedCredentialOffer
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath.using
import com.jayway.jsonpath.Option
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class SaveCredentialImpl @Inject constructor(
    private val credentialOfferRepository: CredentialOfferRepository,
    private val safeJson: SafeJson,
) : SaveCredential {

    override suspend fun invoke(
        issuerInfo: IssuerCredentialInformation,
        anyCredential: AnyCredential,
        credentialConfiguration: AnyCredentialConfiguration,
    ): Result<Long, SaveCredentialError> = coroutineBinding {
        val localizedIssuerDisplays = issuerInfo.display.addFallbackLanguageIfNecessary(issuerInfo.credentialIssuer)
        val localizedCredentialDisplays = credentialConfiguration.display.addFallbackLanguageIfNecessary(credentialConfiguration.identifier)
        val localizedClaims = createLocalizedCredentialClaims(
            credentialConfiguration = credentialConfiguration,
            credential = anyCredential,
        ).bind()
        val localizedCredentialOffer = createLocalizedCredentialOffer(
            credential = anyCredential,
            localizedIssuerDisplays = localizedIssuerDisplays,
            localizedCredentialDisplays = localizedCredentialDisplays,
            localizedClaims = localizedClaims,
        )

        credentialOfferRepository.saveCredentialOffer(
            localizedCredentialOffer = localizedCredentialOffer,
        )
            .mapError(CredentialOfferRepositoryError::toSaveCredentialError)
            .bind()
    }

    private suspend fun createLocalizedCredentialClaims(
        credentialId: Long = -1,
        credentialConfiguration: AnyCredentialConfiguration,
        credential: AnyCredential,
    ): Result<Map<CredentialClaim, List<Display>>, SaveCredentialError> = coroutineBinding {
        val returnMap = mutableMapOf<CredentialClaim, List<Display>>()
        val metadataClaims = getMetadataClaims(credentialConfiguration = credentialConfiguration).bind()

        val conf: Configuration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST).build()

        val json = runSuspendCatching { credential.json }
            .mapError(Throwable::toSaveCredentialError)
            .bind()
        val credentialClaims: Map<String, String> = using(conf)
            .parse(json)
            .read<List<Map<String, JsonElement>>>(credential.claimsPath)
            .firstOrNull()
            ?.mapValues {
                when (it.value) {
                    is JsonPrimitive -> it.value.jsonPrimitive.content
                    is JsonArray -> it.value.jsonArray.toString()
                    is JsonObject -> it.value.jsonObject.toString()
                    is JsonNull -> ""
                }
            } ?: emptyMap()

        metadataClaims.forEach { (claimKey: String, claim: Claim) ->
            val value = credentialClaims[claimKey] ?: return@forEach
            val credentialClaim = CredentialClaim(
                credentialId = credentialId,
                key = claimKey,
                value = value,
                valueType = claim.valueType,
                order = credentialConfiguration.order?.indexOf(claimKey) ?: -1
            )

            val claimDisplays = claim.display.addFallbackLanguageIfNecessary(claimKey)

            returnMap[credentialClaim] = claimDisplays
        }

        returnMap
    }

    private fun getMetadataClaims(
        credentialConfiguration: AnyCredentialConfiguration,
    ): Result<Map<String, Claim>, SaveCredentialError> {
        val jsonString = credentialConfiguration.claims ?: return Err(CredentialError.InvalidMetadataClaims)
        return safeJson.safeDecodeStringTo<Map<String, Claim>>(jsonString)
            .mapError(JsonParsingError::toSaveCredentialError)
    }

    private fun createLocalizedCredentialOffer(
        credential: AnyCredential,
        localizedIssuerDisplays: List<Display>,
        localizedCredentialDisplays: List<Display>,
        localizedClaims: Map<CredentialClaim, List<Display>>,
    ) = LocalizedCredentialOffer(
        privateKeyIdentifier = credential.signingKeyId,
        signingAlgorithm = credential.signingAlgorithm,
        payload = credential.payload,
        format = credential.format,
        issuerDisplays = localizedIssuerDisplays,
        credentialDisplays = localizedCredentialDisplays,
        claims = localizedClaims,
    )

    private fun List<Display>?.addFallbackLanguageIfNecessary(name: String): List<Display> {
        if (this == null) return listOf(Display(name = name, locale = DisplayLanguage.FALLBACK))
        return if (none { it.locale == DisplayLanguage.FALLBACK }) {
            this + Display(name = name, locale = DisplayLanguage.FALLBACK)
        } else {
            this
        }
    }
}
