package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import ch.admin.foitt.openid4vc.domain.model.AnyCredentialConfigurationListSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IssuerCredentialInformation(
    @SerialName("credential_endpoint")
    val credentialEndpoint: String,
    @SerialName("credential_issuer")
    val credentialIssuer: String,
    @SerialName("credential_response_encryption")
    val credentialResponseEncryption: CredentialResponseEncryption?,
    @Serializable(with = AnyCredentialConfigurationListSerializer::class)
    @SerialName("credential_configurations_supported")
    val credentialConfigurations: List<AnyCredentialConfiguration>,
    @SerialName("display")
    val display: List<Display>
)

@Serializable
data class CredentialResponseEncryption(
    @SerialName("alg_values_supported")
    val algValuesSupported: List<String>,
    @SerialName("enc_values_supported")
    val encValuesSupported: List<String>,
    @SerialName("encryption_required")
    val encryptionRequired: Boolean
)

@Serializable(with = AnyCredentialConfigurationSerializer::class)
sealed class AnyCredentialConfiguration {
    @SerialName("identifier")
    abstract val identifier: String

    @SerialName("format")
    open val format: CredentialFormat = CredentialFormat.UNKNOWN

    @SerialName("scope")
    abstract val scope: String?

    abstract val cryptographicBindingMethodsSupported: List<String>?
    abstract val credentialSigningAlgValuesSupported: List<SigningAlgorithm>
    abstract val proofTypesSupported: Map<ProofType, ProofTypeSigningAlgorithms>
    abstract val display: List<Display>?
    abstract val order: List<String>?
    abstract val claims: String?
}

@Serializable
enum class SigningAlgorithm(val stdName: String) {
    @SerialName("ES512")
    ES512("ES512"),

    @SerialName("ES256")
    ES256("ES256"),
}

@Serializable
enum class CredentialFormat(val format: String) {
    @SerialName("vc+sd-jwt")
    VC_SD_JWT("vc+sd-jwt"),

    UNKNOWN("unknown"),
}

@Serializable
enum class ProofType(val type: String) {
    @SerialName("jwt")
    JWT("jwt"),

    UNKNOWN("unknown")
}

@Serializable
data class ProofTypeSigningAlgorithms(
    @SerialName("proof_signing_alg_values_supported")
    val signingAlgorithms: List<SigningAlgorithm>
)

@Serializable
data class Claim(
    @SerialName("mandatory")
    val mandatory: Boolean? = false,
    @SerialName("value_type")
    val valueType: String? = "string",
    @SerialName("display")
    val display: List<Display>? = null
)

// https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-10.2.3.1
@Serializable
data class Display(
    @SerialName("locale")
    val locale: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("logo")
    val logo: Logo? = null,
    @SerialName("name")
    val name: String,
    @SerialName("background_color")
    val backgroundColor: String? = null,
    @SerialName("text_color")
    val textColor: String? = null,
)

@Serializable
data class Logo(
    @SerialName("uri")
    val uri: String? = null,
    @SerialName("alt_text")
    val altText: String? = null,
)
