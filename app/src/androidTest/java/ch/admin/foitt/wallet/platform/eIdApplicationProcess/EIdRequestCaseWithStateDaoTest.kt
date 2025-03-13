package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseWithStateDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestStateDao
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.mock.EIdRequestMocks.eIdRequestCaseMock
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.mock.EIdRequestMocks.eIdRequestStateMock
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class EIdRequestCaseWithStateDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var eIdRequestCaseDao: EIdRequestCaseDao
    private lateinit var eIdRequestStateDao: EIdRequestStateDao
    private lateinit var eIdRequestCaseWithStateDao: EIdRequestCaseWithStateDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        eIdRequestCaseDao = database.eIdRequestCaseDao()
        eIdRequestStateDao = database.eIdRequestStateDao()
        eIdRequestCaseWithStateDao = database.eIdRequestCaseWithStateDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun getEIdRequestCaseWithStateFlowTest() = runTest {
        eIdRequestCaseDao.insert(eIdRequestCaseMock())
        eIdRequestStateDao.insert(eIdRequestStateMock())

        val casesWithStates = eIdRequestCaseWithStateDao.getEIdCasesWithStatesFlow().firstOrNull()

        val expected = listOf(
            EIdRequestCaseWithState(
                case = eIdRequestCaseMock(),
                state = eIdRequestStateMock(),
            )
        )

        assertEquals(expected, casesWithStates)
    }
}
