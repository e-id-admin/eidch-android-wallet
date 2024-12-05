package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText

object MockCredentialClaim {
    const val CLAIM_ID = 1L
    val credentialClaim = CredentialClaim(
        id = CLAIM_ID,
        credentialId = 2,
        key = "key",
        value = "value",
        valueType = "string"
    )
    val credentialClaimDisplay = CredentialClaimDisplay(
        claimId = CLAIM_ID,
        name = "name",
        locale = "xxx"
    )
    val credentialClaimDisplays = listOf(credentialClaimDisplay)
    val credentialClaimText1 = CredentialClaimText("key1", "value2")
    val credentialClaimText2 = CredentialClaimText("key2", "value2")

    fun buildCredentialClaim(valueType: String) = CredentialClaim(
        credentialId = 1,
        key = "key",
        value = "value",
        valueType = valueType
    )
}
