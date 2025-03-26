package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock

import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.toDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus

object MockCredentialWithDisplays {

    const val CREDENTIAL_ID = 5L
    const val CREDENTIAL_ID2 = 6L

    const val CREDENTIAL_ISSUER = "prod issuer"
    const val CREDENTIAL_ISSUER2 = "beta issuer"

    val credentialDisplay1 = CredentialDisplay(
        id = 23,
        credentialId = CREDENTIAL_ID,
        locale = "locale1",
        name = "name1",
        backgroundColor = Color.Black.toString()
    )

    private val credentialDisplay2 = CredentialDisplay(
        id = 24,
        credentialId = CREDENTIAL_ID,
        locale = "locale2",
        name = "name2",
    )

    val credentialDisplay3 = CredentialDisplay(
        id = 25,
        credentialId = CREDENTIAL_ID2,
        locale = "locale1",
        name = "name1",
        backgroundColor = Color.Black.toString()
    )

    private val credentialDisplay4 = CredentialDisplay(
        id = 26,
        credentialId = CREDENTIAL_ID2,
        locale = "locale2",
        name = "name2",
    )

    val credential1Displays = listOf(credentialDisplay1, credentialDisplay2)
    val credential2Displays = listOf(credentialDisplay3, credentialDisplay4)

    val credentialDisplayData1 = CredentialDisplayData(
        credentialId = CREDENTIAL_ID,
        status = CredentialStatus.VALID.toDisplayStatus(),
        credentialDisplay = credentialDisplay1,
        isCredentialFromBetaIssuer = false,
    )

    val credentialDisplayData2 = CredentialDisplayData(
        credentialId = CREDENTIAL_ID2,
        status = CredentialStatus.VALID.toDisplayStatus(),
        credentialDisplay = credentialDisplay3,
        isCredentialFromBetaIssuer = true,
    )
}
