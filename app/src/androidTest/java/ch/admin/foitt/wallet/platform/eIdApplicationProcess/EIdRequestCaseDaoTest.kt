package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseDao
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.mock.EIdRequestMocks.eIdRequestCaseMock
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class EIdRequestCaseDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var eIdRequestCaseDao: EIdRequestCaseDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        eIdRequestCaseDao = database.eIdRequestCaseDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertEIdRequestCaseTest() = runTest {
        eIdRequestCaseDao.insert(eIdRequestCaseMock())

        val case = eIdRequestCaseDao.getEIdRequestCaseById(eIdRequestCaseMock().id)
        assertEquals(eIdRequestCaseMock(), case)
    }
}
