package ch.admin.foitt.openid4vc.domain.model.jwt

import ch.admin.foitt.openid4vc.domain.model.anycredential.JwtValidity
import ch.admin.foitt.openid4vc.domain.model.anycredential.getJwtValidity
import com.nimbusds.jwt.SignedJWT
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.time.Instant

/**
 * https://www.rfc-editor.org/rfc/rfc7519.html
 */
open class Jwt(
    val rawJwt: String,
) {
    val signedJwt: SignedJWT = SignedJWT.parse(rawJwt)
    val payloadString = signedJwt.payload.toString()
    val payloadJson = Json.parseToJsonElement(payloadString).jsonObject

    val algorithm: String = signedJwt.header.algorithm.name
    val type: String? = signedJwt.header.type?.type
    val keyId: String? = signedJwt.header.keyID

    val iss: String? = signedJwt.jwtClaimsSet.issuer

    open val subject: String? = signedJwt.jwtClaimsSet.subject
    open val issuedAt: Instant? = signedJwt.jwtClaimsSet.issueTime?.toInstant()

    val expInstant: Instant? = signedJwt.jwtClaimsSet.expirationTime?.toInstant()
    val nbfInstant: Instant? = signedJwt.jwtClaimsSet.notBeforeTime?.toInstant()

    val jwtValidity: JwtValidity = getJwtValidity(
        issuedAt = issuedAt?.epochSecond,
        validFrom = nbfInstant?.epochSecond,
        validUntil = expInstant?.epochSecond,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Jwt

        return rawJwt == other.rawJwt
    }

    override fun hashCode(): Int {
        return rawJwt.hashCode()
    }
}
