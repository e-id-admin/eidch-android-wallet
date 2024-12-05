package ch.admin.foitt.wallet.platform.ssi.data.source.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialWithDisplaysDao
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplays
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialDisplay2
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CredentialWithDisplaysDaoTest {

    private lateinit var database: AppDatabase

    private lateinit var credentialDao: CredentialDao
    private lateinit var credentialDisplayDao: CredentialDisplayDao
    private lateinit var credentialWithDisplaysDao: CredentialWithDisplaysDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        credentialDisplayDao = database.credentialDisplayDao()
        credentialWithDisplaysDao = database.credentialWithDisplaysDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun getCredentialsWithDisplaysFlowTest() = runTest {
        credentialDao.insert(credential1)
        credentialDisplayDao.insertAll(listOf(credentialDisplay1))

        credentialDao.insert(credential2)
        credentialDisplayDao.insertAll(listOf(credentialDisplay2))

        val credentialsWithDisplays = credentialWithDisplaysDao.getCredentialsWithDisplaysFlow().firstOrNull()

        // credential list is sorted desc by createdAt -> check for credential2 first
        val expected = listOf(
            CredentialWithDisplays(
                credential = credential2,
                displays = listOf(credentialDisplay2)
            ),
            CredentialWithDisplays(
                credential = credential1,
                displays = listOf(credentialDisplay1)
            ),
        )

        assertEquals(expected, credentialsWithDisplays)
    }
}
