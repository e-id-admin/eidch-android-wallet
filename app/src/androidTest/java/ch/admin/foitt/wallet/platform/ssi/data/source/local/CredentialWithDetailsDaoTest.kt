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
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDetails
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaim1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaimDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaimDisplay3
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialDisplay1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialIssuerDisplay1
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CredentialWithDetailsDaoTest {

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
    fun getCredentialWithDetailsFlowByIdTest() = runTest {
        credentialDao.insert(credential1)
        credentialDisplayDao.insertAll(listOf(credentialDisplay1))
        credentialIssuerDisplayDao.insertAll(listOf(credentialIssuerDisplay1))
        credentialClaimDao.insert(credentialClaim1)
        credentialClaimDisplayDao.insertAll(listOf(credentialClaimDisplay1))
        credentialClaimDisplayDao.insertAll(listOf(credentialClaimDisplay3))

        val credentialWithDetails = credentialWithDetailsDao.getCredentialWithDetailsFlowById(credential1.id).firstOrNull()

        val expected = CredentialWithDetails(
            credential = credential1,
            credentialDisplays = listOf(credentialDisplay1),
            issuerDisplays = listOf(credentialIssuerDisplay1),
            claims = listOf(
                CredentialClaimWithDisplays(
                    claim = credentialClaim1,
                    displays = listOf(credentialClaimDisplay1, credentialClaimDisplay3)
                )
            )
        )

        assertEquals(expected, credentialWithDetails)
    }
}
