package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface IdentityTrustStatement {
    @SerialName("sub")
    val sub: String

    @SerialName("status")
    val status: CredentialStatusProperties?

    val entityName: Map<String, String>
    val isStateActor: Boolean
    val registryIds: List<RegistryId>?
}

@Serializable
data class IdentityV1TrustStatement(
    override val iat: Long,
    override val exp: Long?,
    override val vct: String,
    override val sub: String,
    override val status: CredentialStatusProperties,
    override val nbf: Long?,

    @SerialName("entityName")
    override val entityName: Map<String, String>,
    @SerialName("registryIds")
    override val registryIds: List<RegistryId>?,
    @SerialName("isStateActor")
    override val isStateActor: Boolean,
) : IdentityTrustStatement, TrustStatementV1

@Serializable
data class RegistryId(
    @SerialName("type")
    val type: String,
    @SerialName("value")
    val value: String,
)

@Serializable
data class IdentityV2TrustStatement(
    // mandatory for all v2 trust statements
    override val typ: String,
    override val alg: SignatureAlgorithm,
    override val kid: String,
    override val profileVersion: String,
    override val jti: String,
    override val iat: Long,
    override val exp: Long,
    // identity TS also contains
    override val sub: String,
    override val status: CredentialStatusProperties,
    @SerialName("entity_name")
    override val entityName: Map<String, String>,
    @SerialName("is_state_actor")
    override val isStateActor: Boolean,
    @SerialName("registry_ids")
    override val registryIds: List<RegistryId>?,
) : TrustStatementV2, IdentityTrustStatement {
    companion object {
        const val TYPE = "swiyu-identity-trust-statement+jwt"
        const val CLAIM_NAME_ENTITY_NAME = "entity_name"
        const val CLAIM_NAME_REGISTRY_IDS = "registry_ids"
    }
}
