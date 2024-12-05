package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import ch.admin.foitt.openid4vc.domain.model.JsonAsStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VcSdJwtCredentialConfiguration(
    override val identifier: String,
    override val format: CredentialFormat = CredentialFormat.VC_SD_JWT,
    override val scope: String? = null,

    @Serializable
    @SerialName("cryptographic_binding_methods_supported")
    override val cryptographicBindingMethodsSupported: List<String>? = null,
    @Serializable(with = SigningAlgorithmsSerializer::class)
    @SerialName("credential_signing_alg_values_supported")
    override val credentialSigningAlgValuesSupported: List<SigningAlgorithm>,
    @Serializable
    @SerialName("proof_types_supported")
    override val proofTypesSupported: Map<ProofType, ProofTypeSigningAlgorithms> = emptyMap(),
    @SerialName("display")
    override val display: List<Display>? = null,
    @SerialName("order")
    override val order: List<String>? = null,

    @SerialName("vct")
    val vct: String,

    @SerialName("claims")
    @Serializable(with = JsonAsStringSerializer::class)
    override val claims: String? = null,
) : AnyCredentialConfiguration()
