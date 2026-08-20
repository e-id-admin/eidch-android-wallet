package ch.admin.foitt.wallet.platform.ssi.data.source.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.activityActorDisplay1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.activityActorDisplay2
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.activityClaim1
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.credentialActivity2
import ch.admin.foitt.wallet.platform.activityList.mock.ActivityListMocks.image2
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityActorDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.ActivityClaimEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialActivityEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterDisplayEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialIssuerDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.ImageEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialWithDisplaysAndClustersDao
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.cluster1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.clusterDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaim1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaimDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialIssuerDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.verifiableCredential1
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class CredentialDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDao: CredentialDao
    private lateinit var verifiableCredentialDao: VerifiableCredentialDao
    private lateinit var credentialDisplayDao: CredentialDisplayDao
    private lateinit var credentialIssuerDisplayDao: CredentialIssuerDisplayDao
    private lateinit var credentialClaimClusterEntityDao: CredentialClaimClusterEntityDao
    private lateinit var credentialClaimClusterDisplayEntityDao: CredentialClaimClusterDisplayEntityDao
    private lateinit var credentialClaimDao: CredentialClaimDao
    private lateinit var credentialClaimDisplayDao: CredentialClaimDisplayDao

    private lateinit var credentialActivityEntityDao: CredentialActivityEntityDao
    private lateinit var activityClaimEntityDao: ActivityClaimEntityDao
    private lateinit var activityActorDisplayEntityDao: ActivityActorDisplayEntityDao
    private lateinit var imageEntityDao: ImageEntityDao

    private lateinit var credentialWithDisplaysAndClustersDao: VerifiableCredentialWithDisplaysAndClustersDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        verifiableCredentialDao = database.verifiableCredentialDao()
        credentialDisplayDao = database.credentialDisplayDao()
        credentialIssuerDisplayDao = database.credentialIssuerDisplayDao()
        credentialClaimClusterEntityDao = database.credentialClaimClusterEntityDao()
        credentialClaimClusterDisplayEntityDao = database.credentialClaimClusterDisplayEntityDao()
        credentialClaimDao = database.credentialClaimDao()
        credentialClaimDisplayDao = database.credentialClaimDisplayDao()

        credentialActivityEntityDao = database.credentialActivityEntityDao()
        activityClaimEntityDao = database.activityClaimEntityDao()
        activityActorDisplayEntityDao = database.activityActorDisplayEntityDao()
        imageEntityDao = database.imageEntityDao()

        credentialWithDisplaysAndClustersDao = database.verifiableCredentialWithDisplaysAndClustersDao()
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
    fun deleteCredentialTest() = runTest {
        credentialDao.insert(credential1)
        verifiableCredentialDao.insert(verifiableCredential1)
        credentialDisplayDao.insertAll(listOf(credentialDisplay1))
        credentialIssuerDisplayDao.insertAll(listOf(credentialIssuerDisplay1))
        credentialClaimClusterEntityDao.insert(cluster1)
        credentialClaimClusterDisplayEntityDao.insert(clusterDisplay1)
        credentialClaimDao.insert(credentialClaim1)
        credentialClaimDisplayDao.insertAll(listOf(credentialClaimDisplay1))

        credentialActivityEntityDao.insert(credentialActivity2)
        activityClaimEntityDao.insert(activityClaim1)
        imageEntityDao.insert(image2)
        activityActorDisplayEntityDao.insert(activityActorDisplay2)

        credentialDao.deleteById(credential1.id)

        assertNull(
            credentialWithDisplaysAndClustersDao.getNullableVerifiableCredentialWithDisplaysAndClustersFlowById(
                credential1.id
            ).firstOrNull()
        )
        assertEquals(
            emptyList<CredentialIssuerDisplay>(),
            credentialIssuerDisplayDao.getCredentialIssuerDisplaysById(credential1.id)
        )
        assertThrows<Throwable> {
            credentialActivityEntityDao.getById(credentialActivity2.id)
        }
        assertThrows<Throwable> {
            activityClaimEntityDao.getById(activityClaim1.id)
        }
        assertThrows<Throwable> {
            activityActorDisplayEntityDao.getById(activityActorDisplay1.id)
        }
    }
}
