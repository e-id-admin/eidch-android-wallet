package ch.admin.foitt.wallet.platform.ssi.domain.model

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData

data class CredentialDetail(
    val credential: CredentialDisplayData,
    val claims: List<CredentialClaimData>,
)
