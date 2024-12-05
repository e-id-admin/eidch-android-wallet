package ch.admin.foitt.wallet.platform.versionEnforcement.domain.model

sealed interface AppVersionInfo {
    data class Blocked(val title: String?, val text: String?) : AppVersionInfo
    data object Valid : AppVersionInfo
    data object Unknown : AppVersionInfo
}
