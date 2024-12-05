package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay

@Dao
interface CredentialDisplayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(credentialDisplays: Collection<CredentialDisplay>)
}
