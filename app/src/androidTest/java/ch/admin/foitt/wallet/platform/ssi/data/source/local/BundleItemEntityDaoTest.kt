package ch.admin.foitt.wallet.platform.ssi.data.source.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.BundleItemEntityDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.VerifiableCredentialDao
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.verifiableCredential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.verifiableCredential2
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BundleItemEntityDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var credentialDao: CredentialDao
    private lateinit var verifiableCredentialDao: VerifiableCredentialDao
    private lateinit var bundleItemEntityDao: BundleItemEntityDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        credentialDao = database.credentialDao()
        verifiableCredentialDao = database.verifiableCredentialDao()
        bundleItemEntityDao = database.bundleItemEntityDao()

        credentialDao.insert(credential1)
        verifiableCredentialDao.insert(verifiableCredential1)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun freshBatchReportsFullCount() = runTest {
        insertBundleItems(credentialId = credential1.id, amount = BATCH_SIZE)

        val count = bundleItemEntityDao.getNeverPresentedCountByCredentialId(credential1.id)

        assertEquals("A fresh batch should report all items as never presented", BATCH_SIZE, count)
    }

    @Test
    fun presentedItemsAreNotCountedAsNeverPresented() = runTest {
        val bundleItemIds = insertBundleItems(credentialId = credential1.id, amount = BATCH_SIZE)

        // present all except for one item
        bundleItemIds.dropLast(1).forEach { bundleItemId ->
            bundleItemEntityDao.onPresented(bundleItemId)
        }

        val count = bundleItemEntityDao.getNeverPresentedCountByCredentialId(credential1.id)

        // Batch refresh triggers when count falls under threshold (20%) or less.
        // If presented items are still counted, the count never drops and a batch refresh is never triggered.
        assertEquals("Presented bundle items must not be counted as never presented", 1, count)
    }

    @Test
    fun exhaustedBatchReportsZeroNeverPresentedItems() = runTest {
        val bundleItemIds = insertBundleItems(credentialId = credential1.id, amount = BATCH_SIZE)

        bundleItemIds.forEach { bundleItemId ->
            bundleItemEntityDao.onPresented(bundleItemId)
        }

        val count = bundleItemEntityDao.getNeverPresentedCountByCredentialId(credential1.id)

        // If a batch was completely presented we still have to be able to trigger a refresh
        assertEquals("A fully presented batch should report zero never presented items", 0, count)
    }

    @Test
    fun countsAreReportedPerCredential() = runTest {
        credentialDao.insert(credential2)
        verifiableCredentialDao.insert(verifiableCredential2)

        val bundleItemIds = insertBundleItems(credentialId = credential1.id, amount = BATCH_SIZE)
        insertBundleItems(credentialId = credential2.id, amount = 6)

        // present all items of the first credential
        bundleItemIds.forEach { bundleItemId ->
            bundleItemEntityDao.onPresented(bundleItemId)
        }

        assertEquals(
            "Presenting items of one credential must not affect the count of another credential",
            6,
            bundleItemEntityDao.getNeverPresentedCountByCredentialId(credential2.id),
        )
        assertEquals(0, bundleItemEntityDao.getNeverPresentedCountByCredentialId(credential1.id))
    }

    private fun insertBundleItems(credentialId: Long, amount: Int): List<Long> =
        (1..amount).map { index ->
            bundleItemEntityDao.insert(
                BundleItemEntity(
                    credentialId = credentialId,
                    payload = "payload$index",
                )
            )
        }

    private companion object {
        const val BATCH_SIZE = 10
    }
}
