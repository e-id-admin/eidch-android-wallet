package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class VcSdJwtCredentialRequest(
    @SerialName("credential_definition")
    val credentialDefinition: CredentialDefinition,
    @SerialName("format")
    val format: CredentialFormat,
    @SerialName("vct")
    val vct: String,
    // Only send the field proof in the request if it contains a proof
    @EncodeDefault(NEVER)
    @SerialName("proof")
    val proof: CredentialRequestProof? = null
) : AnyCredentialRequest()
