package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialWithDetailsDao {
    @Transaction
    @Query("SELECT * FROM credential WHERE id = :id")
    fun getCredentialWithDetailsFlowById(id: Long): Flow<CredentialWithDetails?>
}
