package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.usecase.IsBetaIssuer
import javax.inject.Inject

class IsBetaIssuerImpl @Inject constructor() : IsBetaIssuer {
    private val betaIssuerPattern =
        Regex(pattern = "^([^:]+:){3}identifier-reg(-.)?\\.trust-infra\\.swiyu-int\\.admin\\.ch:.*")

    override suspend fun invoke(credentialIssuer: String?): Boolean = credentialIssuer?.let {
        betaIssuerPattern.containsMatchIn(credentialIssuer)
    } ?: false
}
