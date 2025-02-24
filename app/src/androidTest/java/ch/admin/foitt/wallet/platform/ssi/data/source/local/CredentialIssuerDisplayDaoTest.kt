package ch.admin.foitt.wallet.platform.ssi.data.source.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialIssuerDisplayDao
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialIssuerDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialIssuerDisplay2
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CredentialIssuerDisplayDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDao: CredentialDao
    private lateinit var credentialIssuerDisplayDao: CredentialIssuerDisplayDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        credentialDao.insert(credential1)
        credentialDao.insert(credential2)

        credentialIssuerDisplayDao = database.credentialIssuerDisplayDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertWithoutMatchingForeignKeyShouldThrow() = runTest {
        credentialIssuerDisplayDao.insertAll(listOf(credentialIssuerDisplay1.copy(credentialId = -1)))
    }

    @Test
    fun getCredentialIssuerDisplaysByIdTest() = runTest {
        credentialDao.insert(credential1)
        credentialDao.insert(credential2)
        credentialIssuerDisplayDao.insertAll(listOf(credentialIssuerDisplay1, credentialIssuerDisplay2))

        val issuerDisplays = credentialIssuerDisplayDao.getCredentialIssuerDisplaysById(credential2.id)

        assertEquals(credentialIssuerDisplay2, issuerDisplays.first())
    }
}
