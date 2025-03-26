package ch.admin.foitt.wallet.platform.database.data.dao

import kotlinx.coroutines.flow.StateFlow

interface DaoProvider {
    val credentialDaoFlow: StateFlow<CredentialDao?>
    val credentialDisplayDaoFlow: StateFlow<CredentialDisplayDao?>
    val credentialClaimDaoFlow: StateFlow<CredentialClaimDao?>
    val credentialClaimDisplayDaoFlow: StateFlow<CredentialClaimDisplayDao?>
    val credentialIssuerDisplayDaoFlow: StateFlow<CredentialIssuerDisplayDao?>
    val credentialWithDisplaysAndClaimsDaoFlow: StateFlow<CredentialWithDisplaysAndClaimsDao?>
    val credentialWithDisplaysDaoFlow: StateFlow<CredentialWithDisplaysDao?>
    val eIdRequestCaseDaoFlow: StateFlow<EIdRequestCaseDao?>
    val eIdRequestStateDaoFlow: StateFlow<EIdRequestStateDao?>
    val eIdRequestCaseWithStateDaoFlow: StateFlow<EIdRequestCaseWithStateDao?>
}
