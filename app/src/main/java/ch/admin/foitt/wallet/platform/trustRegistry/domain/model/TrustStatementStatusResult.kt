package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties

data class TrustStatementStatusResult(
    val status: CredentialStatusProperties,
    val kid: String,
)
