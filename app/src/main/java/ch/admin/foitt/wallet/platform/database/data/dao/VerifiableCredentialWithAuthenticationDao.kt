package ch.admin.foitt.wallet.platform.database.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialWithAuthentication

@Dao
interface VerifiableCredentialWithAuthenticationDao {
    @Transaction
    @Query("SELECT * FROM verifiableCredentialEntity")
    fun getAll(): List<VerifiableCredentialWithAuthentication>

    @Transaction
    @Query("SELECT * FROM verifiableCredentialEntity WHERE credentialId = :credentialId")
    fun getById(credentialId: Long): VerifiableCredentialWithAuthentication
}
