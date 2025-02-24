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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
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
        val metadataClaims = getMetadataClaims(credentialConfiguration = credentialConfiguration).bind()

        val credentialJson = runSuspendCatching { credential.json }
            .mapError(Throwable::toSaveCredentialError)
            .bind()
        val conf: Configuration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST).build()
        val credentialClaims: Map<String, String> = using(conf)
            .parse(credentialJson)
            .read<List<Map<String, JsonElement>>>(credential.claimsPath)
            .firstOrNull()
            ?.mapValues {
                when (it.value) {
                    is JsonPrimitive -> it.value.jsonPrimitive.contentOrNull ?: ""
                    is JsonArray -> it.value.jsonArray.toString()
                    is JsonObject -> it.value.jsonObject.toString()
                }
            } ?: emptyMap()

        credentialClaims
            .filterNot { reserved_claim_names.contains(it.key) }
            .map { (claimKey, claimValue) ->
                val metadata = metadataClaims[claimKey]
                val credentialClaim = CredentialClaim(
                    credentialId = credentialId,
                    key = claimKey,
                    value = claimValue,
                    valueType = metadata?.valueType ?: DEFAULT_VALUE_TYPE,
                    order = credentialConfiguration.order?.indexOf(claimKey) ?: -1
                )
                val claimDisplays = metadata?.display.addFallbackLanguageIfNecessary(claimKey)
                credentialClaim to claimDisplays
            }.toMap()
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
        keyBindingIdentifier = credential.keyBindingIdentifier,
        keyBindingAlgorithm = credential.keyBindingAlgorithm,
        payload = credential.payload,
        format = credential.format,
        issuerDisplays = localizedIssuerDisplays,
        credentialDisplays = localizedCredentialDisplays,
        claims = localizedClaims,
        issuer = credential.issuer
    )

    private fun List<Display>?.addFallbackLanguageIfNecessary(name: String): List<Display> {
        if (this == null) {
            return listOf(Display(name = name, locale = DisplayLanguage.FALLBACK))
        }

        return if (none { it.locale == DisplayLanguage.FALLBACK }) {
            this + Display(name = name, locale = DisplayLanguage.FALLBACK)
        } else {
            this
        }
    }

    companion object {
        // Reserved claim names
        // See https://www.ietf.org/archive/id/draft-ietf-oauth-sd-jwt-vc-04.html#name-registered-jwt-claims and
        // https://www.ietf.org/archive/id/draft-ietf-oauth-selective-disclosure-jwt-10.html#section-5.1
        private val reserved_claim_names =
            listOf("iss", "nbf", "exp", "cnf", "vct", "status", "sub", "iat", "_sd_alg", "_sd")
        private const val DEFAULT_VALUE_TYPE = "string"
    }
}
