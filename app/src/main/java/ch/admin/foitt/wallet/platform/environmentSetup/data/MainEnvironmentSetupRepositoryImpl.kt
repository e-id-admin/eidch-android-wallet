package ch.admin.foitt.wallet.platform.environmentSetup.data

import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import javax.inject.Inject

class MainEnvironmentSetupRepositoryImpl @Inject constructor() : EnvironmentSetupRepository {
    override val appVersionEnforcementUrl: String = "https://wallet-ve.trust-infra.swiyu.admin.ch/v1/android"

    override val trustRegistryMapping: Map<String, String> = mapOf(
        "identifier-reg.trust-infra.swiyu.admin.ch" to "trust-reg.trust-infra.swiyu.admin.ch",
        "identifier-reg.trust-infra.swiyu-int.admin.ch" to "trust-reg.trust-infra.swiyu-int.admin.ch"
    )

    override val trustedDids: List<String> = listOf("to be defined")

    override val baseTrustDomainRegex =
        Regex("^did:tdw:[^:]+:([^:]+\\.swiyu(-int)?\\.admin\\.ch):[^:]+", setOf(RegexOption.MULTILINE))
}
