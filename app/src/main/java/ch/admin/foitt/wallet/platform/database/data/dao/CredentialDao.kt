package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus

@Dao
interface CredentialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(credential: Credential): Long

    @Query("UPDATE credential SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    fun updateStatusByCredentialId(id: Long, status: CredentialStatus, updatedAt: Long): Int

    @Query("DELETE FROM Credential WHERE id = :id")
    fun deleteById(id: Long)

    @Query("SELECT * FROM Credential WHERE id = :id")
    fun getById(id: Long): Credential

    @Query("SELECT * FROM Credential ORDER BY createdAt DESC")
    fun getAll(): List<Credential>

    @Query("SELECT id FROM Credential")
    fun getAllIds(): List<Long>
}
