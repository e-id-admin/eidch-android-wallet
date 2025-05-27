package ch.admin.foitt.wallet.platform.database

import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialIssuerDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialWithDisplaysAndClaimsDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialWithDisplaysDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseWithStateDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestStateDao
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplays
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialWithPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeDaoProviderImpl : DaoProvider {
    override val credentialDaoFlow: StateFlow<CredentialDao?>
        get() = MutableStateFlow(object : CredentialDao {
            override fun insert(credential: Credential): Long {
                return 1L
            }

            override fun updateStatusByCredentialId(id: Long, status: CredentialStatus, updatedAt: Long): Int {
                return 1
            }

            override fun deleteById(id: Long) {
                // No-op in the mock
            }

            override fun getById(id: Long): Credential {
                return credentialWithPayload
            }

            override fun getAll(): List<Credential> {
                return listOf(
                    credential1,
                    credential2,
                    credentialWithPayload
                )
            }

            override fun getAllIds(): List<Long> {
                return listOf(1L, 2L, 3L)
            }
        })
    override val credentialDisplayDaoFlow: StateFlow<CredentialDisplayDao?>
        get() = MutableStateFlow(null)
    override val credentialClaimDaoFlow: StateFlow<CredentialClaimDao?>
        get() = MutableStateFlow(null)
    override val credentialClaimDisplayDaoFlow: StateFlow<CredentialClaimDisplayDao?>
        get() = MutableStateFlow(null)
    override val credentialIssuerDisplayDaoFlow: StateFlow<CredentialIssuerDisplayDao?>
        get() = MutableStateFlow(null)
    override val credentialWithDisplaysAndClaimsDaoFlow: StateFlow<CredentialWithDisplaysAndClaimsDao?>
        get() = MutableStateFlow(null)
    override val credentialWithDisplaysDaoFlow: StateFlow<CredentialWithDisplaysDao?>
        get() = MutableStateFlow(object : CredentialWithDisplaysDao {
            override fun getCredentialsWithDisplaysFlow(): Flow<List<CredentialWithDisplays>> {
                return MutableStateFlow(
                    listOf(
                        CredentialWithDisplays(
                            credential = credentialWithPayload,
                            displays = listOf(credentialDisplay1)
                        )
                    )
                )
            }
        })
    override val eIdRequestCaseDaoFlow: StateFlow<EIdRequestCaseDao?>
        get() = MutableStateFlow(null)
    override val eIdRequestStateDaoFlow: StateFlow<EIdRequestStateDao?>
        get() = MutableStateFlow(null)
    override val eIdRequestCaseWithStateDaoFlow: StateFlow<EIdRequestCaseWithStateDao?>
        get() = MutableStateFlow(object : EIdRequestCaseWithStateDao {
            override fun getEIdCasesWithStatesFlow(): Flow<List<EIdRequestCaseWithState>> {
                return MutableStateFlow(listOf())
            }
        })
}
