package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties
import kotlinx.serialization.SerialName

sealed interface TrustStatement {
    @SerialName("iat")
    val iat: Long

    @SerialName("exp")
    val exp: Long?

    companion object {
        const val CLAIM_NAME_STATUS = "status"
    }
}

sealed interface TrustStatementV1 : TrustStatement {
    override val iat: Long
    override val exp: Long?

    @SerialName("vct")
    val vct: String

    @SerialName("sub")
    val sub: String

    @SerialName("status")
    val status: CredentialStatusProperties?

    @SerialName("nbf")
    val nbf: Long?
}

sealed interface TrustStatementV2 : TrustStatement {
    val typ: String
    val alg: SignatureAlgorithm
    val kid: String
    val profileVersion: String

    @SerialName("jti")
    val jti: String
    override val iat: Long
    override val exp: Long

    companion object {
        const val CLAIM_NAME_JTI = "jti"
        const val CLAIM_NAME_STATE_ACTOR = "is_state_actor"
        const val CLAIM_NAME_REGISTRY_ID = "registry_ids"
        const val CLAIM_NAME_PROFILE_VERSION = "profile_version"
    }
}
