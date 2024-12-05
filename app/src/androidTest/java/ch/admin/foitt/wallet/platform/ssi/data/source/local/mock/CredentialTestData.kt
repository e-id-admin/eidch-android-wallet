package ch.admin.foitt.wallet.platform.ssi.data.source.local.mock

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage

object CredentialTestData {
    private const val PAYLOAD = "PAYLOAD"
    private val FORMAT = CredentialFormat.VC_SD_JWT
    const val VALUE = "VALUE"
    private const val VALUE2 = "VALUE2"
    const val KEY = "KEY"
    private const val KEY2 = "KEY2"
    const val NAME1 = "NAME1"
    const val CORRECT = "CORRECT"
    const val FALLBACK = "FALLBACK"

    private const val NAME2 = "NAME2"
    private const val NAME3 = "NAME3"
    private const val IDENTIFIER = "IDENTIFIER"
    private const val SIGNING_ALGORITHM = "SIGNING_ALGORITHM"
    private const val LOGO_DATA = "logo data"

    val credential1 = Credential(
        id = 1,
        status = CredentialStatus.VALID,
        privateKeyIdentifier = IDENTIFIER,
        payload = PAYLOAD,
        format = FORMAT,
        createdAt = 1,
        updatedAt = 1,
        signingAlgorithm = SIGNING_ALGORITHM,
    )
    val credential2 = Credential(
        id = 2,
        status = CredentialStatus.VALID,
        privateKeyIdentifier = IDENTIFIER,
        payload = PAYLOAD,
        format = FORMAT,
        createdAt = 2,
        updatedAt = 2,
        signingAlgorithm = SIGNING_ALGORITHM,
    )

    val credentialClaim1 = CredentialClaim(id = 1, credentialId = 1, key = KEY, value = VALUE, valueType = null)
    val credentialClaim2 = CredentialClaim(id = 2, credentialId = 2, key = KEY2, value = VALUE2, valueType = null)

    val credentialClaimDisplay1 = CredentialClaimDisplay(id = 1, claimId = credentialClaim1.id, name = NAME1, locale = "xx")
    val credentialClaimDisplay2 = CredentialClaimDisplay(id = 2, claimId = credentialClaim2.id, name = NAME2, locale = "xx_XX")
    val credentialClaimDisplay3 = CredentialClaimDisplay(id = 3, claimId = credentialClaim1.id, name = NAME3, locale = "xx_XX")

    val credentialDisplay1 = CredentialDisplay(id = 1, credentialId = credential1.id, locale = "xx_XX", name = CORRECT)
    val credentialDisplay2 = CredentialDisplay(
        id = 2,
        credentialId = credential2.id,
        locale = DisplayLanguage.FALLBACK,
        name = FALLBACK
    )

    val credentialIssuerDisplay1 = CredentialIssuerDisplay(id = 1, credentialId = credential1.id, name = NAME1, locale = "xx")
    val credentialIssuerDisplay2 = CredentialIssuerDisplay(id = 2, credentialId = credential2.id, name = NAME2, locale = "xx_XX")
}
