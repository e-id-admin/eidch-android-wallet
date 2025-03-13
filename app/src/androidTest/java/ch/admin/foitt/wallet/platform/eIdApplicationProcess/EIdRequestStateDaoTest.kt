package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestStateDao
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.mock.EIdRequestMocks.eIdRequestCaseMock
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.mock.EIdRequestMocks.eIdRequestStateMock
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.Instant

class EIdRequestStateDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var eIdRequestCaseDao: EIdRequestCaseDao
    private lateinit var eIdRequestStateDao: EIdRequestStateDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        eIdRequestCaseDao = database.eIdRequestCaseDao()
        eIdRequestStateDao = database.eIdRequestStateDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertEIdRequestStateTest() = runTest {
        eIdRequestCaseDao.insert(eIdRequestCaseMock())
        val id = eIdRequestStateDao.insert(eIdRequestStateMock())

        val requestState = eIdRequestStateDao.getEIdRequestStateById(id)
        assertEquals(eIdRequestStateMock(), requestState)
    }

    @Test
    fun updateEIdRequestStateTest() = runTest {
        eIdRequestCaseDao.insert(eIdRequestCaseMock())
        val id = eIdRequestStateDao.insert(eIdRequestStateMock())

        val newState = EIdRequestQueueState.CLOSED
        val lastPolled = Instant.now().epochSecond
        eIdRequestStateDao.updateByCaseId(eIdRequestCaseMock().id, newState, lastPolled)

        val updatedRequestState = eIdRequestStateDao.getEIdRequestStateById(id)
        assertEquals(newState, updatedRequestState.state)
        assertEquals(lastPolled, updatedRequestState.lastPolled)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertWithoutMatchingForeignKeyShouldThrow() {
        eIdRequestStateDao.insert(eIdRequestStateMock())
    }

    @Test
    fun getAllStateCaseIds() = runTest {
        eIdRequestCaseDao.insert(eIdRequestCaseMock())
        eIdRequestStateDao.insert(eIdRequestStateMock())

        eIdRequestCaseDao.insert(eIdRequestCaseMock("caseId2"))
        eIdRequestStateDao.insert(eIdRequestStateMock(id = 2L, caseId = "caseId2"))

        val caseIds = eIdRequestStateDao.getAllStateCaseIds()

        assertEquals(2, caseIds.size)
    }
}
