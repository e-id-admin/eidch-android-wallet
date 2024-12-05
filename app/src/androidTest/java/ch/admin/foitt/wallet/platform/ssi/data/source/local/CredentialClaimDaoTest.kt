package ch.admin.foitt.wallet.platform.ssi.data.source.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.KEY
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.VALUE
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential2
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CredentialClaimDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDao: CredentialDao
    private lateinit var credentialClaimDao: CredentialClaimDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        credentialDao.insert(credential1)
        credentialDao.insert(credential2)
        credentialClaimDao = database.credentialClaimDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertCredentialClaimTest() = runTest {
        assertEquals(
            emptyList<CredentialClaim>(),
            credentialClaimDao.getByCredentialId(credentialId = credential1.id)
        )

        val credentialClaim =
            CredentialClaim(id = 1, credentialId = credential1.id, key = KEY, value = VALUE, valueType = null)
        val id = credentialClaimDao.insert(credentialClaim)

        assertEquals(
            listOf(credentialClaim.copy(id = id)),
            credentialClaimDao.getByCredentialId(credentialId = credential1.id)
        )

        credentialClaimDao.insert(credentialClaim.copy(id = 0))
        assertEquals(
            2,
            credentialClaimDao.getByCredentialId(credentialId = credential1.id).size
        )
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertWithoutMatchingForeignKeyShouldThrow() {
        val credentialClaim = CredentialClaim(id = 1, credentialId = -1, key = KEY, value = VALUE, valueType = null)
        credentialClaimDao.insert(credentialClaim)
    }
}
