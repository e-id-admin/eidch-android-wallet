package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCaseWallet

@Dao
interface EIdRequestCaseWalletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(wallet: EIdRequestCaseWallet): Long

    @Query("SELECT * FROM eidrequestcasewallet WHERE eIdRequestCaseId = :caseId")
    fun getWalletsByCaseId(caseId: String): List<EIdRequestCaseWallet>

    @Query("DELETE FROM eidrequestcasewallet WHERE eIdRequestCaseId = :caseId")
    fun deleteByCaseId(caseId: String)
}
