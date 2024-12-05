package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplays
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialWithDisplaysDao {
    @Transaction
    @Query("SELECT * FROM credential ORDER BY createdAt DESC")
    fun getCredentialsWithDisplaysFlow(): Flow<List<CredentialWithDisplays>>
}
