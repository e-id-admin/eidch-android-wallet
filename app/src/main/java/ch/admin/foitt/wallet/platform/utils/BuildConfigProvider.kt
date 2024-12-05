package ch.admin.foitt.wallet.platform.utils

interface BuildConfigProvider {
    val appVersion: AppVersion
    val appVersionEnforcementUrl: String
    val trustRegistryMapping: Map<String, String>
    val trustedDids: List<String>
}
