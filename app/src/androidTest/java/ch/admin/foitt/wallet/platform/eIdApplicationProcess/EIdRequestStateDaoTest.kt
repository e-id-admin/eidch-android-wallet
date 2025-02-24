package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestStateDao
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.mock.EIdRequestMocks.eIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.mock.EIdRequestMocks.eIdRequestState
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
        eIdRequestCaseDao.insert(eIdRequestCase)
        val id = eIdRequestStateDao.insert(eIdRequestState)

        val requestState = eIdRequestStateDao.getEIdRequestStateById(id)
        assertEquals(eIdRequestState, requestState)
    }

    @Test
    fun updateEIdRequestStateTest() = runTest {
        eIdRequestCaseDao.insert(eIdRequestCase)
        val id = eIdRequestStateDao.insert(eIdRequestState)

        val newState = EIdRequestQueueState.Abgelaufen
        val lastPolled = Instant.now().epochSecond
        eIdRequestStateDao.updateByCaseId(eIdRequestCase.id, newState, lastPolled)

        val updatedRequestState = eIdRequestStateDao.getEIdRequestStateById(id)
        assertEquals(newState, updatedRequestState.state)
        assertEquals(lastPolled, updatedRequestState.lastPolled)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertWithoutMatchingForeignKeyShouldThrow() {
        eIdRequestStateDao.insert(eIdRequestState)
    }
}
