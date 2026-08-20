package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimClusterEntity

@Dao
interface CredentialClaimClusterEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cluster: CredentialClaimClusterEntity): Long

    @Query("SELECT * FROM credentialclaimclusterentity WHERE id = :id")
    fun getById(id: Long): CredentialClaimClusterEntity?

    @Query("DELETE FROM credentialclaimclusterentity WHERE verifiableCredentialId = :credentialId")
    fun deleteByCredentialId(credentialId: Long): Int
}
