package ch.admin.foitt.openid4vc.utils

import ch.admin.foitt.openid4vc.domain.model.anycredential.CredentialValidity
import com.nimbusds.jwt.JWT
import java.time.Instant

val JWT.validity: CredentialValidity
    get() {
        val notBefore = jwtClaimsSet.notBeforeTime?.toInstant()?.minusSeconds(BUFFER) ?: Instant.MIN
        val notAfter = jwtClaimsSet.expirationTime?.toInstant()?.plusSeconds(BUFFER) ?: Instant.MAX
        val now = Instant.now()
        return when {
            now.isBefore(notBefore) -> CredentialValidity.NOT_YET_VALID
            now.isAfter(notAfter) -> CredentialValidity.EXPIRED
            else -> CredentialValidity.VALID
        }
    }

private const val BUFFER = 15L
