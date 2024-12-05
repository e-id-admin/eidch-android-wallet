package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim

@Dao
interface CredentialClaimDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(credentialClaim: CredentialClaim): Long

    @Query("SELECT * FROM CredentialClaim WHERE credentialId = :credentialId ORDER BY `order`")
    fun getByCredentialId(credentialId: Long): List<CredentialClaim>
}
