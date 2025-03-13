package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState

@Dao
interface EIdRequestStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(state: EIdRequestState): Long

    @Query("SELECT * FROM eidrequeststate WHERE id = :id")
    fun getEIdRequestStateById(id: Long): EIdRequestState

    @Query(
        "UPDATE eidrequeststate SET state = :state, lastPolled = :lastPolled, " +
            "onlineSessionStartTimeoutAt = :onlineSessionStartTimeout, onlineSessionStartOpenAt = :onlineSessionStartOpenAt " +
            "WHERE eIdRequestCaseId = :caseId"
    )
    fun updateByCaseId(
        caseId: String,
        state: EIdRequestQueueState,
        lastPolled: Long,
        onlineSessionStartTimeout: Long?,
        onlineSessionStartOpenAt: Long?
    ): Int

    @Query("SELECT eIdRequestCaseId FROM eidrequeststate")
    fun getAllStateCaseIds(): List<String>
}
