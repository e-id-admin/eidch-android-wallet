package ch.admin.foitt.openid4vc.domain.model.jwt

import ch.admin.foitt.openid4vc.domain.model.anycredential.CredentialValidity
import com.nimbusds.jwt.SignedJWT
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.time.Instant

/**
 * https://www.rfc-editor.org/rfc/rfc7519.html
 */
open class Jwt(
    rawJwt: String,
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

    val expiredAt: Instant? = signedJwt.jwtClaimsSet.expirationTime?.toInstant()
    val notBefore: Instant? = signedJwt.jwtClaimsSet.notBeforeTime?.toInstant()

    val jwtValidity: CredentialValidity
        get() {
            val notBeforeWithLeeway = notBefore?.minusSeconds(LEEWAY)
            val expiredAtWithLeeway = expiredAt?.plusSeconds(LEEWAY)
            val now = Instant.now()
            return when {
                notBeforeWithLeeway != null && now.isBefore(notBeforeWithLeeway) -> CredentialValidity.NotYetValid(notBefore)
                expiredAtWithLeeway != null && now.isAfter(expiredAtWithLeeway) -> CredentialValidity.Expired(expiredAt)
                else -> CredentialValidity.Valid
            }
        }

    private companion object {
        const val LEEWAY = 15L
    }
}
