package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UnknownCredentialConfiguration(
    override val identifier: String = "",
    override val format: CredentialFormat = CredentialFormat.UNKNOWN,
    override val scope: String? = null,

    @Serializable
    @SerialName("cryptographic_binding_methods_supported")
    override val cryptographicBindingMethodsSupported: List<String>? = null,
    @Serializable(with = SigningAlgorithmsSerializer::class)
    @SerialName("credential_signing_alg_values_supported")
    override val credentialSigningAlgValuesSupported: List<SigningAlgorithm> = emptyList(),
    @Serializable
    @SerialName("proof_types_supported")
    override val proofTypesSupported: Map<ProofType, ProofTypeSigningAlgorithms> =
        mapOf(ProofType.UNKNOWN to ProofTypeSigningAlgorithms(signingAlgorithms = emptyList())),
    @SerialName("display")
    override val display: List<OidCredentialDisplay>? = null,
    @SerialName("order")
    override val order: List<String>? = null,

    @SerialName("claims")
    override val claims: String? = null,
) : AnyCredentialConfiguration()
