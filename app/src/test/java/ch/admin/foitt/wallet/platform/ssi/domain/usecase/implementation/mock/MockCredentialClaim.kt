package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays

object MockCredentialClaim {
    private const val CLAIM_ID = 1L

    val credentialClaimDisplay = CredentialClaimDisplay(
        claimId = CLAIM_ID,
        name = "name",
        locale = "xxx"
    )
    val credentialClaimDisplays = listOf(credentialClaimDisplay)

    fun buildClaimWithDisplays(
        valueType: String,
        displays: List<CredentialClaimDisplay> = credentialClaimDisplays
    ) = CredentialClaimWithDisplays(
        claim = CredentialClaim(
            credentialId = 1,
            key = "key",
            value = "value",
            valueType = valueType
        ),
        displays = displays,
    )
}
