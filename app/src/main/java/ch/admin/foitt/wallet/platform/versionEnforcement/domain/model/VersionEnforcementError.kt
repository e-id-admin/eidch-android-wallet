package ch.admin.foitt.wallet.platform.versionEnforcement.domain.model

interface VersionEnforcementError {
    data class Unexpected(val throwable: Throwable?) : FetchVersionEnforcementError
}

sealed interface FetchVersionEnforcementError
