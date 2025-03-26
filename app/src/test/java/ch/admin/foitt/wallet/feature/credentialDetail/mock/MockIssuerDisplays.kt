package ch.admin.foitt.wallet.feature.credentialDetail.mock

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay

object MockIssuerDisplays {

    const val CREDENTIAL_ID = 5L

    val issuerDisplay1 = CredentialIssuerDisplay(
        id = 33,
        credentialId = CREDENTIAL_ID,
        locale = "locale1",
        name = "name1",
    )

    private val issuerDisplay2 = CredentialIssuerDisplay(
        id = 34,
        credentialId = CREDENTIAL_ID,
        locale = "locale2",
        name = "name2",
    )

    val issuerDisplays = listOf(issuerDisplay1, issuerDisplay2)
}
