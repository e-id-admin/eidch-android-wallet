package ch.admin.foitt.openid4vc.domain.model.anycredential

import java.time.Instant

sealed interface Validity {
    data object Valid : JwtValidity, CredentialValidity
    data class NotYetValid(val validFrom: Instant) : JwtValidity, CredentialValidity
    data class Expired(val expiredAt: Instant) : JwtValidity, CredentialValidity

    data object IssuedInFuture : JwtValidity

    data class BusinessExpired(val expiredAt: Instant) : CredentialValidity

    companion object {
        const val LEEWAY = 15L
    }
}

sealed interface CredentialValidity : Validity
sealed interface JwtValidity : Validity

fun getCredentialValidity(
    validFrom: Long?,
    validUntil: Long?,
    businessExpiredValidity: Instant? = null,
): CredentialValidity {
    val validFromInstant = validFrom?.let { Instant.ofEpochSecond(it) }
    val validFromWithLeeway = validFromInstant?.minusSeconds(Validity.LEEWAY)
    val validUntilInstant = validUntil?.let { Instant.ofEpochSecond(it) }
    val now = Instant.now()

    return when {
        validFromWithLeeway != null && now.isBefore(validFromWithLeeway) -> Validity.NotYetValid(validFromInstant)
        validUntilInstant != null && now.isAfter(validUntilInstant) -> Validity.Expired(validUntilInstant)
        businessExpiredValidity != null && now.isAfter(businessExpiredValidity) -> Validity.BusinessExpired(businessExpiredValidity)
        else -> Validity.Valid
    }
}

fun getJwtValidity(
    issuedAt: Long?,
    validFrom: Long?,
    validUntil: Long?,
): JwtValidity {
    val issuedAtInstant = issuedAt?.let { Instant.ofEpochSecond(it) }
    val issuedAtWithLeeway = issuedAtInstant?.minusSeconds(Validity.LEEWAY)
    val validFromInstant = validFrom?.let { Instant.ofEpochSecond(it) }
    val validFromWithLeeway = validFromInstant?.minusSeconds(Validity.LEEWAY)
    val validUntilInstant = validUntil?.let { Instant.ofEpochSecond(it) }
    val now = Instant.now()

    return when {
        issuedAtWithLeeway != null && now.isBefore(issuedAtWithLeeway) -> Validity.IssuedInFuture
        validFromWithLeeway != null && now.isBefore(validFromWithLeeway) -> Validity.NotYetValid(validFromInstant)
        validUntilInstant != null && now.isAfter(validUntilInstant) -> Validity.Expired(validUntilInstant)
        else -> Validity.Valid
    }
}
