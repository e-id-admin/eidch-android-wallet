package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestStateDao
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.LegalRepresentativeConsent
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.mock.EIdRequestMocks.eIdRequestCaseMock
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.mock.EIdRequestMocks.eIdRequestStateMock
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
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
        val onlineSessionStartOpenAt = Instant.now().epochSecond
        val onlineSessionStartTimeout = Instant.now().epochSecond
        val legalRepresentativeConsent = LegalRepresentativeConsent.NOT_VERIFIED
        eIdRequestStateDao.updateByCaseId(EIdRequestState(
            eIdRequestCaseId = eIdRequestCaseMock().id,
            state = newState,
            lastPolled = lastPolled,
            onlineSessionStartOpenAt = onlineSessionStartOpenAt,
            onlineSessionStartTimeoutAt = onlineSessionStartTimeout,
            legalRepresentativeConsent = legalRepresentativeConsent,
        ))

        val updatedRequestState = eIdRequestStateDao.getEIdRequestStateById(id)
        assertEquals(newState, updatedRequestState?.state)
        assertEquals(lastPolled, updatedRequestState?.lastPolled)
        assertEquals(onlineSessionStartOpenAt, updatedRequestState?.onlineSessionStartOpenAt)
        assertEquals(onlineSessionStartTimeout, updatedRequestState?.onlineSessionStartTimeoutAt)
        assertEquals(legalRepresentativeConsent, updatedRequestState?.legalRepresentativeConsent)

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

    @Test
    fun deleteEIdRequestCaseTestWillDeleteState() = runTest {
        insertEIdRequestStateTest()
        eIdRequestCaseDao.deleteById(eIdRequestCaseMock().id)

        assertThrows<Throwable> {
            eIdRequestCaseDao.getEIdRequestCaseById(eIdRequestCaseMock().id)
            eIdRequestStateDao.getEIdRequestStateById(eIdRequestStateMock().id)
        }
    }
}
