package ch.admin.foitt.wallet.feature.credentialDetail.domain.model

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData

data class CredentialDetail(
    val credential: CredentialPreview,
    val claims: List<CredentialClaimData>,
    val issuer: CredentialIssuerDisplay,
)
