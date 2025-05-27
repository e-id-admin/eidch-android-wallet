package ch.admin.foitt.wallet.platform.database

import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.domain.model.ChangeDatabasePassphraseError
import ch.admin.foitt.wallet.platform.database.domain.model.CreateDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseState
import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credential2
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaim1
import ch.admin.foitt.wallet.platform.ssi.data.source.local.mock.CredentialTestData.credentialClaim2
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.StateFlow

class FakeDatabaseRepositoryImpl(
    private val appDatabase: AppDatabase,
    override val databaseState: StateFlow<DatabaseState>
) : DatabaseRepository {

    private lateinit var credentialDao: CredentialDao
    private lateinit var credentialClaimDao: CredentialClaimDao
    private lateinit var credentialClaimDisplayDao: CredentialClaimDisplayDao

    override suspend fun createDatabase(passphrase: ByteArray): Result<Unit, CreateDatabaseError> {
        credentialDao = appDatabase.credentialDao()
        credentialDao.insert(credential1)
        credentialDao.insert(credential2)

        credentialClaimDao = appDatabase.credentialClaimDao()
        credentialClaimDao.insert(credentialClaim1)
        credentialClaimDao.insert(credentialClaim2)
        credentialClaimDisplayDao = appDatabase.credentialClaimDisplayDao()
        return Ok(Unit)
    }

    override suspend fun close() {
        appDatabase.close()
    }

    override suspend fun open(passphrase: ByteArray): Result<Unit, OpenDatabaseError> {
        return Ok(Unit)
    }

    override suspend fun checkIfCorrectPassphrase(passphrase: ByteArray): Result<Unit, OpenDatabaseError> {
        return Ok(Unit)
    }

    override suspend fun changePassphrase(newPassphrase: ByteArray): Result<Unit, ChangeDatabasePassphraseError> {
        return Ok(Unit)
    }

    override fun isOpen(): Boolean {
        return true
    }

    override suspend fun <V> runInTransaction(block: suspend () -> V): V? = block()
}
