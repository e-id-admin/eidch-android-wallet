package ch.admin.foitt.openid4vc.domain.model.anycredential

import java.time.Instant

sealed interface CredentialValidity {
    data class NotYetValid(val validFrom: Instant) : CredentialValidity
    data object Valid : CredentialValidity
    data class Expired(val expiredAt: Instant) : CredentialValidity
}
