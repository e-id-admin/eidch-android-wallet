package ch.admin.foitt.wallet.platform.environmentSetup.domain.repository

interface EnvironmentSetupRepository {
    val appVersionEnforcementUrl: String
    val trustRegistryMapping: Map<String, String>
    val trustedDids: List<String>
    val baseTrustDomainRegex: Regex
}
