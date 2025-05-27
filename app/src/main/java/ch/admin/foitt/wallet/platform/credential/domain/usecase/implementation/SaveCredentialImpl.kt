package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Claim
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialInformationDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInformation
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidClaimDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidCredentialDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.SaveCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toSaveCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayConst
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
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
        val localizedIssuerDisplays: List<OidIssuerDisplay> = issuerInfo.display.addFallbackLanguageIfNecessary {
            OidIssuerDisplay(name = DisplayConst.ISSUER_FALLBACK_NAME, locale = DisplayLanguage.FALLBACK)
        }

        val localizedCredentialDisplays = credentialConfiguration.display.addFallbackLanguageIfNecessary {
            OidCredentialDisplay(name = credentialConfiguration.identifier, locale = DisplayLanguage.FALLBACK)
        }
        val localizedClaims = createLocalizedCredentialClaims(
            credentialConfiguration = credentialConfiguration,
            credential = anyCredential,
        ).bind()

        credentialOfferRepository.saveCredentialOffer(
            keyBindingIdentifier = anyCredential.keyBindingIdentifier,
            keyBindingAlgorithm = anyCredential.keyBindingAlgorithm,
            payload = anyCredential.payload,
            format = anyCredential.format,
            validFrom = anyCredential.validFromInstant?.epochSecond,
            validUntil = anyCredential.validUntilInstant?.epochSecond,
            issuer = anyCredential.issuer,
            issuerDisplays = localizedIssuerDisplays,
            credentialDisplays = localizedCredentialDisplays,
            claims = localizedClaims,
        )
            .mapError(CredentialOfferRepositoryError::toSaveCredentialError)
            .bind()
    }

    private suspend fun createLocalizedCredentialClaims(
        credentialId: Long = -1,
        credentialConfiguration: AnyCredentialConfiguration,
        credential: AnyCredential,
    ): Result<Map<CredentialClaim, List<OidClaimDisplay>>, SaveCredentialError> = coroutineBinding {
        // Map<Attribute, Claim>, e.g. mapOf("family_name" to Claim)
        val metadataClaims = getMetadataClaims(credentialConfiguration = credentialConfiguration).bind()

        val credentialJson = runSuspendCatching { credential.getClaimsToSave() }
            .mapError { throwable -> throwable.toSaveCredentialError("getClaimsToSave error") }
            .bind()
        val conf: Configuration = Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST).build()
        // Map<JsonPath, value>
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

        credentialClaims.map { (claimKey, claimValue) ->
            val metadata = metadataClaims[claimKey]
            val credentialClaim = CredentialClaim(
                credentialId = credentialId,
                key = claimKey,
                value = claimValue,
                valueType = metadata?.valueType ?: DEFAULT_VALUE_TYPE,
                order = credentialConfiguration.order?.indexOf(claimKey) ?: -1
            )
            val claimDisplays: List<OidClaimDisplay> = metadata?.display.addFallbackLanguageIfNecessary {
                OidClaimDisplay(name = claimKey, locale = DisplayLanguage.FALLBACK)
            }
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

    private fun <T : CredentialInformationDisplay> List<T>?.addFallbackLanguageIfNecessary(
        fallbackValue: () -> T
    ): List<T> {
        if (this == null) {
            return listOf(fallbackValue())
        }

        return if (none { it.locale == DisplayLanguage.FALLBACK }) {
            this + fallbackValue()
        } else {
            this
        }
    }

    companion object {
        private const val DEFAULT_VALUE_TYPE = "string"
    }
}
