package ch.admin.foitt.wallet.platform.environmentSetup.data

import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import javax.inject.Inject

class MainEnvironmentSetupRepositoryImpl @Inject constructor() : EnvironmentSetupRepository {
    override val appVersionEnforcementUrl: String = "https://wallet-ve.trust-infra.swiyu.admin.ch/v1/android"

    override val trustRegistryMapping: Map<String, String> = mapOf(
        "identifier-reg.trust-infra.swiyu.admin.ch" to "trust-reg.trust-infra.swiyu.admin.ch",
        "identifier-reg.trust-infra.swiyu-int.admin.ch" to "trust-reg.trust-infra.swiyu-int.admin.ch"
    )

    @Suppress("MaximumLineLength")
    override val trustedDids: List<String> = listOf(
        "did:tdw:QmWrXWFEDenvoYWFXxSQGFCa6Pi22Cdsg2r6weGhY2ChiQ:identifier-reg.trust-infra.swiyu-int.admin.ch:api:v1:did:2e246676-209a-4c21-aceb-721f8a90b212",
    )

    override val baseTrustDomainRegex =
        Regex("^did:tdw:[^:]+:([^:]+\\.swiyu(-int)?\\.admin\\.ch):[^:]+", setOf(RegexOption.MULTILINE))

    override val betaIdRequestEnabled = true

    override val eIdRequestEnabled = false

    override val sidBackendUrl: String = "https://eid.admin.ch"

    override val fetchOca = false
}
