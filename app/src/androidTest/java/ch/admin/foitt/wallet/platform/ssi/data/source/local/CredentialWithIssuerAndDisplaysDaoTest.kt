package ch.admin.foitt.wallet.platform.ssi.data.source.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialIssuerDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialWithIssuerAndDisplaysDao
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithIssuerAndDisplays
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialDisplay2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialIssuerDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialIssuerDisplay2
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CredentialWithIssuerAndDisplaysDaoTest {

    private lateinit var database: AppDatabase

    private lateinit var credentialDao: CredentialDao
    private lateinit var credentialDisplayDao: CredentialDisplayDao
    private lateinit var credentialIssuerDisplayDao: CredentialIssuerDisplayDao
    private lateinit var credentialWithIssuerAndDisplaysDao: CredentialWithIssuerAndDisplaysDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        credentialDisplayDao = database.credentialDisplayDao()
        credentialIssuerDisplayDao = database.credentialIssuerDisplayDao()
        credentialWithIssuerAndDisplaysDao = database.credentialWithIssuerAndDisplaysDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun getCredentialsWithIssuerAndDisplaysFlowTest() = runTest {
        credentialDao.insert(credential1)
        credentialDisplayDao.insertAll(listOf(credentialDisplay1))
        credentialIssuerDisplayDao.insertAll(listOf(credentialIssuerDisplay1))

        credentialDao.insert(credential2)
        credentialDisplayDao.insertAll(listOf(credentialDisplay2))
        credentialIssuerDisplayDao.insertAll(listOf(credentialIssuerDisplay2))

        val credentialsWithIssuerAndDisplays = credentialWithIssuerAndDisplaysDao.getCredentialsWithIssuerAndDisplaysFlow().firstOrNull()

        val expected = listOf(
            CredentialWithIssuerAndDisplays(
                credential = credential2,
                credentialDisplays = listOf(credentialDisplay2),
                issuerDisplays = listOf(credentialIssuerDisplay2),
            ),
            CredentialWithIssuerAndDisplays(
                credential = credential1,
                credentialDisplays = listOf(credentialDisplay1),
                issuerDisplays = listOf(credentialIssuerDisplay1),
            ),
        )

        assertEquals(expected, credentialsWithIssuerAndDisplays)
    }

    @Test
    fun getCredentialWithIssuerAndDisplaysFlowByIdTest() = runTest {
        credentialDao.insert(credential1)
        credentialDisplayDao.insertAll(listOf(credentialDisplay1))
        credentialIssuerDisplayDao.insertAll(listOf(credentialIssuerDisplay1))

        val credentialWithIssuerAndDisplays =
            credentialWithIssuerAndDisplaysDao.getCredentialWithIssuerAndDisplaysFlowById(credential1.id).firstOrNull()

        val expected = CredentialWithIssuerAndDisplays(
            credential = credential1,
            credentialDisplays = listOf(credentialDisplay1),
            issuerDisplays = listOf(credentialIssuerDisplay1),
        )

        assertEquals(expected, credentialWithIssuerAndDisplays)
    }
}
