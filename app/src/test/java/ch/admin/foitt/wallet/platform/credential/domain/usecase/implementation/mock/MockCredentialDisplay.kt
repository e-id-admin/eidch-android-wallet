package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay

object MockCredentialDisplay {
    val credentialDisplay = CredentialDisplay(
        id = 1,
        credentialId = 1,
        locale = "locale",
        name = "name",
    )
}
