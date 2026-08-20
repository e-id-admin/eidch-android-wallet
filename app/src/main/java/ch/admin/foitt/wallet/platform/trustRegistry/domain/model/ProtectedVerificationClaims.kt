package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

data class ProtectedVerificationClaims(val claims: Set<String> = setOf("personal_administrative_number"))
