package ch.admin.foitt.wallet.platform.ssi.data.source.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialIssuerDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialWithDetailsDao
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaim1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaimDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialIssuerDisplay1
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CredentialDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDao: CredentialDao
    private lateinit var credentialDisplayDao: CredentialDisplayDao
    private lateinit var credentialIssuerDisplayDao: CredentialIssuerDisplayDao
    private lateinit var credentialClaimDao: CredentialClaimDao
    private lateinit var credentialClaimDisplayDao: CredentialClaimDisplayDao

    private lateinit var credentialWithDetailsDao: CredentialWithDetailsDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        credentialDisplayDao = database.credentialDisplayDao()
        credentialIssuerDisplayDao = database.credentialIssuerDisplayDao()
        credentialClaimDao = database.credentialClaimDao()
        credentialClaimDisplayDao = database.credentialClaimDisplayDao()

        credentialWithDetailsDao = database.credentialWithDetailsDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertCredentialTest() = runTest {
        val id = credentialDao.insert(credential1)

        var credentials = credentialDao.getAll()
        assertEquals("There is 1 credential in the db", 1, credentials.size)
        assertEquals(credentials.first(), credentialDao.getById(id))

        val updatedId = credentialDao.insert(credential1)
        credentials = credentialDao.getAll()
        assertEquals("Inserting the same credential replaces the old one", 1, credentials.size)
        assertEquals(credentials.first(), credentialDao.getById(updatedId))
    }

    @Test
    fun updateCredentialStatusByCredentialIdTest() = runTest {
        val id = credentialDao.insert(credential1)
        val newStatus = CredentialStatus.REVOKED
        val updatedAt = 2L

        credentialDao.updateStatusByCredentialId(id, newStatus, updatedAt)

        val credential = credentialDao.getById(id)
        assertEquals("Credential status should be updated", newStatus, credential.status)
        assertEquals("UpdatedAt should be updated", updatedAt, credential.updatedAt)
    }

    @Test
    fun deleteCredentialTest() = runTest {
        credentialDao.insert(credential1)
        credentialDisplayDao.insertAll(listOf(credentialDisplay1))
        credentialIssuerDisplayDao.insertAll(listOf(credentialIssuerDisplay1))
        credentialClaimDao.insert(credentialClaim1)
        credentialClaimDisplayDao.insertAll(listOf(credentialClaimDisplay1))

        credentialDao.deleteById(credential1.id)

        assertNull(credentialWithDetailsDao.getCredentialWithDetailsFlowById(credential1.id).firstOrNull())
    }
}
