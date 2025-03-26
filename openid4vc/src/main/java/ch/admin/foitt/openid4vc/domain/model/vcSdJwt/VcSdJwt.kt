package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.recoverCatching
import com.nimbusds.jwt.JWTClaimNames
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

/**
 * https://www.ietf.org/archive/id/draft-ietf-oauth-sd-jwt-vc-04.html
 */
open class VcSdJwt(
    rawVcSdJwt: String,
) : SdJwt(rawVcSdJwt) {
    val vcIssuer: String = iss ?: error("missing iss claim")
    val kid: String = keyId ?: error("missing keyId claim")
    val vct = sdJwtJson.jsonObject[CLAIM_KEY_VCT]?.jsonPrimitive?.content ?: error("missing vct claim")
    val vctIntegrity: String? = sdJwtJson.jsonObject[CLAIM_KEY_VCT_INTEGRITY]?.jsonPrimitive?.content
    val cnf = sdJwtJson.jsonObject[CLAIM_KEY_CNF]
    val status = sdJwtJson.jsonObject[CLAIM_KEY_STATUS]

    /* "sub" claim can optionally be put in disclosures, so it has to be read here */
    override val subject: String? = runSuspendCatching {
        sdJwtJson.jsonObject[JWTClaimNames.SUBJECT]?.jsonPrimitive?.content
    }.get()

    /* "iat" claim can optionally be put in disclosures, so it has to be read here */
    override val issuedAt: Instant? = runSuspendCatching {
        sdJwtJson.jsonObject[JWTClaimNames.ISSUED_AT]?.jsonPrimitive?.content
    }.get()?.toInstant()

    private fun String.toInstant(): Instant? = runSuspendCatching {
        Instant.ofEpochSecond(this.toLong())
    }.recoverCatching {
        Instant.parse(this)
    }.get()

    private companion object {
        const val CLAIM_KEY_CNF = "cnf"
        const val CLAIM_KEY_VCT = "vct"
        const val CLAIM_KEY_VCT_INTEGRITY = "vct#integrity"
        const val CLAIM_KEY_STATUS = "status"
    }
}
