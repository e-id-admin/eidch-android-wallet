package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay

@Dao
interface CredentialIssuerDisplayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(credentialIssuerDisplays: Collection<CredentialIssuerDisplay>)

    @Query("SELECT * FROM credentialissuerdisplay WHERE credentialId = :credentialId")
    fun getCredentialIssuerDisplaysById(credentialId: Long): List<CredentialIssuerDisplay>
}
