package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.wallet.platform.database.domain.model.BatchRefreshDataEntity

@Dao
interface BatchRefreshDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(batchRefreshDataEntity: BatchRefreshDataEntity): Long

    @Query("UPDATE BatchRefreshDataEntity SET batchSize = :batchSize WHERE credentialId = :credentialId")
    fun updateBatchSize(credentialId: Long, batchSize: BatchSize): Int

    @Query("SELECT * FROM BatchRefreshDataEntity WHERE credentialId = :credentialId")
    fun getByCredentialId(credentialId: Long): BatchRefreshDataEntity?
}
