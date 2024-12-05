package ch.admin.foitt.wallet.platform.ssi.data.source.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDisplayDao
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialDisplay1
import org.junit.After
import org.junit.Before
import org.junit.Test

class CredentialDisplayDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDisplayDao: CredentialDisplayDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDisplayDao = database.credentialDisplayDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertWithoutMatchingForeignKeyShouldThrow() {
        credentialDisplayDao.insertAll(listOf(credentialDisplay1))
    }
}
