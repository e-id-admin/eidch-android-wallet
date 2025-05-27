package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState

@Dao
interface EIdRequestStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(state: EIdRequestState): Long

    @Query("SELECT * FROM eidrequeststate WHERE id = :id")
    fun getEIdRequestStateById(id: Long): EIdRequestState

    @Query("SELECT * FROM eidrequeststate WHERE eIdRequestCaseId = :caseId")
    fun getEIdRequestStateByCaseId(caseId: String): EIdRequestState

    @Update
    fun update(state: EIdRequestState): Int

    fun updateByCaseId(state: EIdRequestState): Int {
        val eIdRequestState = getEIdRequestStateByCaseId(state.eIdRequestCaseId)
        return update(state.copy(id = eIdRequestState.id))
    }

    @Query("SELECT eIdRequestCaseId FROM eidrequeststate")
    fun getAllStateCaseIds(): List<String>
}
