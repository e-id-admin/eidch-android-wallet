package ch.admin.foitt.wallet.platform.database.data

import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialIssuerDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialWithDisplaysAndClaimsDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialWithDisplaysDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestCaseWithStateDao
import ch.admin.foitt.wallet.platform.database.data.dao.EIdRequestStateDao
import ch.admin.foitt.wallet.platform.database.domain.model.ChangeDatabasePassphraseError
import ch.admin.foitt.wallet.platform.database.domain.model.CreateDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseState
import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DatabaseWrapper @Inject constructor(
    @IoDispatcherScope private val coroutineScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val databaseInitializer: DatabaseInitializer,
) : DatabaseRepository, DaoProvider {

    @TestOnly
    internal var mutexOwner: Any? = null
    private val databaseMutex = Mutex()
    private val database: MutableStateFlow<AppDatabase?> = MutableStateFlow(null)

    override val databaseState = database.map { currentDatabase ->
        when {
            currentDatabase != null && currentDatabase.isOpen && currentDatabase.tryDecrypt().isOk -> DatabaseState.OPEN
            else -> DatabaseState.CLOSED
        }
    }.stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        initialValue = DatabaseState.CLOSED,
    )

    override suspend fun createDatabase(
        passphrase: ByteArray,
    ): Result<Unit, CreateDatabaseError> = withContext(ioDispatcher) {
        databaseMutex.withLock(mutexOwner) {
            coroutineBinding {
                val createdDatabase = databaseInitializer.create(passphrase).bind()
                // The database is not actually written as long as we don't do anything with it.
                createdDatabase.tryDecrypt().mapError { error ->
                    createdDatabase.close()
                    DatabaseError.SetupFailed(error.throwable)
                }.bind()

                database.update { createdDatabase }
            }
        }
    }

    override suspend fun close() = withContext(ioDispatcher) {
        databaseMutex.withLock(mutexOwner) {
            Timber.d("Close database")
            database.value?.close()
            database.update { null }
        }
    }

    override suspend fun open(
        passphrase: ByteArray,
    ): Result<Unit, OpenDatabaseError> = withContext(ioDispatcher) {
        databaseMutex.withLock(mutexOwner) {
            Timber.d("Try to open database")
            coroutineBinding {
                if (database.value != null) Err(DatabaseError.AlreadyOpen).bind<OpenDatabaseError>()
                val createdDatabase = databaseInitializer.create(passphrase).bind()

                createdDatabase.tryDecrypt()
                    .onSuccess {
                        database.update { createdDatabase }
                    }.onFailure {
                        createdDatabase.close()
                        database.update { null }
                    }.bind()
            }
        }
    }

    override suspend fun checkIfCorrectPassphrase(
        passphrase: ByteArray
    ): Result<Unit, OpenDatabaseError> = withContext(ioDispatcher) {
        databaseMutex.withLock(mutexOwner) {
            coroutineBinding {
                val createdDatabase = databaseInitializer.create(passphrase).bind()
                createdDatabase.tryDecrypt()
                    .onFailure {
                        // do not close the current instance at this point
                        Timber.d("wrong passphrase")
                    }.bind()
                createdDatabase.close()
            }
        }
    }

    override suspend fun changePassphrase(newPassphrase: ByteArray): Result<Unit, ChangeDatabasePassphraseError> =
        withContext(ioDispatcher) {
            databaseMutex.withLock(mutexOwner) {
                database.value?.changePassword(newPassphrase)
                    ?: Err(DatabaseError.ReKeyFailed(IllegalStateException("Database is not open")))
            }
        }

    override fun isOpen(): Boolean {
        return database.value != null
    }

    private fun <T> getDaoFlow(mapper: (AppDatabase?) -> T?): StateFlow<T?> = database.map { mapper(it) }.stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        initialValue = null,
    )

    //region DAOs
    override val credentialDaoFlow: StateFlow<CredentialDao?> = getDaoFlow { it?.credentialDao() }
    override val credentialDisplayDaoFlow: StateFlow<CredentialDisplayDao?> = getDaoFlow { it?.credentialDisplayDao() }
    override val credentialClaimDaoFlow: StateFlow<CredentialClaimDao?> = getDaoFlow { it?.credentialClaimDao() }
    override val credentialClaimDisplayDaoFlow: StateFlow<CredentialClaimDisplayDao?> =
        getDaoFlow { it?.credentialClaimDisplayDao() }
    override val credentialIssuerDisplayDaoFlow: StateFlow<CredentialIssuerDisplayDao?> =
        getDaoFlow { it?.credentialIssuerDisplayDao() }
    override val credentialWithDisplaysAndClaimsDaoFlow: StateFlow<CredentialWithDisplaysAndClaimsDao?> =
        getDaoFlow { it?.credentialWithDisplaysAndClaimsDao() }
    override val credentialWithDisplaysDaoFlow: StateFlow<CredentialWithDisplaysDao?> =
        getDaoFlow { it?.credentialWithDisplaysDao() }
    override val eIdRequestCaseDaoFlow: StateFlow<EIdRequestCaseDao?> =
        getDaoFlow { it?.eIdRequestCaseDao() }
    override val eIdRequestStateDaoFlow: StateFlow<EIdRequestStateDao?> = getDaoFlow { it?.eIdRequestStateDao() }
    override val eIdRequestCaseWithStateDaoFlow: StateFlow<EIdRequestCaseWithStateDao?> = getDaoFlow {
        it?.eIdRequestCaseWithStateDao()
    }
    //endregion
}
