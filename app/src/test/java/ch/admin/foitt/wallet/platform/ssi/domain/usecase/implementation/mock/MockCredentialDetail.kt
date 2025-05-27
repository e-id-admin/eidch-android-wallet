package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.toDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialDetail

object MockCredentialDetail {

    const val CREDENTIAL_ID = 5L
    private const val CLAIM_ID_1 = 1L
    private const val CLAIM_ID_2 = 2L
    private const val ISSUER = "issuer"

    private val credential = Credential(
        id = CREDENTIAL_ID,
        status = CredentialStatus.VALID,
        keyBindingIdentifier = "privateKeyIdentifier",
        payload = "payload",
        format = CredentialFormat.VC_SD_JWT,
        keyBindingAlgorithm = "signingAlgorithm",
        issuer = ISSUER,
        validFrom = 0,
        validUntil = 17768026519L,
    )

    @OptIn(ExperimentalStdlibApi::class)
    private val credentialDisplay1 = CredentialDisplay(
        id = 23,
        credentialId = CREDENTIAL_ID,
        locale = "locale1",
        name = "name1",
        backgroundColor = "#" + Color.Black.toArgb().toHexString()
    )

    private val credentialDisplay2 = CredentialDisplay(
        id = 24,
        credentialId = CREDENTIAL_ID,
        locale = "locale2",
        name = "name2",
    )

    val credentialDisplays = listOf(credentialDisplay1, credentialDisplay2)

    private val claim1 = CredentialClaim(
        id = CLAIM_ID_1,
        credentialId = CREDENTIAL_ID,
        key = "key1",
        value = "value1",
        valueType = "valueType1",
        order = 0,
    )

    private val claim2 = CredentialClaim(
        id = CLAIM_ID_2,
        credentialId = CREDENTIAL_ID,
        key = "key2",
        value = "value2",
        valueType = "valueType2",
        order = 1,
    )

    private val claimDisplay1 = CredentialClaimDisplay(
        id = 0,
        claimId = CLAIM_ID_1,
        name = "name1",
        locale = "locale1",
    )

    private val claimDisplay2 = CredentialClaimDisplay(
        id = 1,
        claimId = CLAIM_ID_1,
        name = "name2",
        locale = "locale2",
    )

    private val claimDisplay3 = CredentialClaimDisplay(
        id = 2,
        claimId = CLAIM_ID_2,
        name = "name3",
        locale = "locale3",
    )

    private val claimDisplay4 = CredentialClaimDisplay(
        id = 3,
        claimId = CLAIM_ID_2,
        name = "name4",
        locale = "locale4",
    )

    val claimWithDisplays1 = CredentialClaimWithDisplays(
        claim = claim1,
        displays = listOf(
            claimDisplay1,
            claimDisplay2,
        )
    )

    val claimWithDisplays2 = CredentialClaimWithDisplays(
        claim = claim2,
        displays = listOf(
            claimDisplay3,
            claimDisplay4,
        )
    )

    val claims = listOf(claimWithDisplays1, claimWithDisplays2)

    val claimData1 = CredentialClaimText(
        localizedKey = claimDisplay1.name,
        value = claim1.value,
    )

    val claimData2 = CredentialClaimText(
        localizedKey = claimDisplay3.name,
        value = claim2.value,
    )

    val credentialDisplayData = CredentialDisplayData(
        credentialId = credential.id,
        status = credential.status.toDisplayStatus(),
        credentialDisplay = credentialDisplay1,
        isCredentialFromBetaIssuer = false
    )

    val credentialDetail = CredentialDetail(
        credential = credentialDisplayData,
        claims = listOf(claimData1, claimData2),
    )

    val credentialDetail2 = CredentialDetail(
        credential = credentialDisplayData,
        claims = listOf(claimData1, claimData2),
    )
}
