package ch.admin.foitt.wallet.feature.credentialOffer.mock

import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOffer
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.toDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimCluster
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail

object MockCredentialOffer {

    const val CREDENTIAL_ID = 5L
    const val ISSUER = "issuer"

    private val credentialDisplay1 = CredentialDisplay(
        id = 23,
        credentialId = CREDENTIAL_ID,
        locale = "locale1",
        name = "name1",
        backgroundColor = "#ff000000"
    )

    private val credentialDisplay2 = CredentialDisplay(
        id = 24,
        credentialId = CREDENTIAL_ID,
        locale = "locale2",
        name = "name2",
    )

    val credentialDisplays = listOf(credentialDisplay1, credentialDisplay2)

    val credentialDisplayData = CredentialDisplayData(
        credentialId = CREDENTIAL_ID,
        status = CredentialStatus.VALID.toDisplayStatus(),
        credentialDisplay = credentialDisplay1,
        actorEnvironment = ActorEnvironment.PRODUCTION,
        progressionState = VerifiableProgressionState.ACCEPTED,
        createdAt = 1,
    )

    val listOfCredentialClaimCluster = listOf(
        CredentialClaimCluster(
            id = 1,
            order = 1,
            localizedLabel = "label",
            parentId = null,
            items = mutableListOf(MockCredentialDetail.claimData1, MockCredentialDetail.claimData2),
        )
    )

    val credentialOffer = CredentialOffer(
        credential = credentialDisplayData,
        claims = listOfCredentialClaimCluster
    )

    val credentialOffer2 = CredentialOffer(
        credential = credentialDisplayData,
        claims = listOfCredentialClaimCluster
    )
}
