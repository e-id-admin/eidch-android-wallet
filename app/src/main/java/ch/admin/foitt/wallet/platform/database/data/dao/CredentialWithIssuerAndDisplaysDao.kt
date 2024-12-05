package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithIssuerAndDisplays
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialWithIssuerAndDisplaysDao {
    @Transaction
    @Query("SELECT * FROM credential ORDER BY id DESC")
    fun getCredentialsWithIssuerAndDisplaysFlow(): Flow<List<CredentialWithIssuerAndDisplays>>

    @Transaction
    @Query("SELECT * FROM credential WHERE id = :id")
    fun getCredentialWithIssuerAndDisplaysFlowById(id: Long): Flow<CredentialWithIssuerAndDisplays?>
}
