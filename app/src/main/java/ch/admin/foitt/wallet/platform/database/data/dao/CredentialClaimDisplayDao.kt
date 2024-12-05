package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay

@Dao
interface CredentialClaimDisplayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(credentialClaimDisplays: Collection<CredentialClaimDisplay>)

    @Query("SELECT * FROM CredentialClaimDisplay WHERE claimId = :claimId")
    fun getByClaimId(claimId: Long): List<CredentialClaimDisplay>
}
