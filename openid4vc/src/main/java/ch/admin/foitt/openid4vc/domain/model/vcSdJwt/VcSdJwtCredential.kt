package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.CredentialValidity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import ch.admin.foitt.openid4vc.utils.validity
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOr
import kotlinx.serialization.json.JsonElement

class VcSdJwtCredential(
    override val id: Long? = null,
    override val signingKeyId: String?,
    override val signingAlgorithm: SigningAlgorithm?,
    override val payload: String,
) : AnyCredential {

    private val sdJwt: SdJwt by lazy {
        SdJwt(payload)
    }

    override val format: CredentialFormat = CredentialFormat.VC_SD_JWT

    override val validity: CredentialValidity
        get() = runSuspendCatching { sdJwt.signedJWT.validity }.getOr(CredentialValidity.EXPIRED)

    override val json: JsonElement by lazy {
        sdJwt.json
    }

    override val claimsPath = "$"

    override fun createVerifiableCredential(requestedFieldKeys: List<String>): String =
        sdJwt.createSelectiveDisclosure(requestedFieldKeys)
}
