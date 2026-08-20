package ch.admin.foitt.wallet.platform.ssi.data.source.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimClusterEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.DISPLAY_VALUE
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.NAME1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.cluster1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.cluster2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaim1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaim2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaimDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaimDisplay2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.verifiableCredential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.verifiableCredential2
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CredentialClaimDisplayDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDao: CredentialDao
    private lateinit var verifiableCredentialDao: VerifiableCredentialDao
    private lateinit var credentialClaimClusterDao: CredentialClaimClusterEntityDao
    private lateinit var credentialClaimDao: CredentialClaimDao
    private lateinit var credentialClaimDisplayDao: CredentialClaimDisplayDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        credentialDao.insert(credential1)
        credentialDao.insert(credential2)

        verifiableCredentialDao = database.verifiableCredentialDao()
        verifiableCredentialDao.insert(verifiableCredential1)
        verifiableCredentialDao.insert(verifiableCredential2)

        credentialClaimClusterDao = database.credentialClaimClusterEntityDao()
        credentialClaimClusterDao.insert(cluster1)
        credentialClaimClusterDao.insert(cluster2)

        credentialClaimDao = database.credentialClaimDao()
        credentialClaimDao.insert(credentialClaim1)
        credentialClaimDao.insert(credentialClaim2)
        credentialClaimDisplayDao = database.credentialClaimDisplayDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAndGetCredentialClaimDisplayTest() = runTest {
        val credentialClaimDisplays = listOf(
            credentialClaimDisplay1,
            credentialClaimDisplay2
        )
        credentialClaimDisplayDao.insertAll(credentialClaimDisplays)

        val displays = credentialClaimDisplayDao.getByClaimId(credentialClaim1.id)

        assertEquals(listOf(credentialClaimDisplay1), displays)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertWithoutMatchingForeignKeyShouldThrow() {
        credentialClaimDisplayDao.insertAll(
            listOf(
                CredentialClaimDisplay(id = 1, claimId = -1, name = NAME1, locale = "xx_XX", value = DISPLAY_VALUE)
            )
        )
    }
}
