package ch.admin.foitt.wallet.platform.database

import android.content.Context
import androidx.room.Room
import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.di.DatabaseBindModule
import ch.admin.foitt.wallet.platform.database.di.DatabaseModule
import ch.admin.foitt.wallet.platform.database.domain.model.ChangeDatabasePassphraseError
import ch.admin.foitt.wallet.platform.database.domain.model.CreateDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseState
import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.database.domain.usecase.ChangeDatabasePassphrase
import ch.admin.foitt.wallet.platform.database.domain.usecase.CheckDatabasePassphrase
import ch.admin.foitt.wallet.platform.database.domain.usecase.CloseAppDatabase
import ch.admin.foitt.wallet.platform.database.domain.usecase.CreateAppDatabase
import ch.admin.foitt.wallet.platform.database.domain.usecase.IsAppDatabaseOpen
import ch.admin.foitt.wallet.platform.database.domain.usecase.OpenAppDatabase
import ch.admin.foitt.wallet.platform.database.domain.usecase.RunInTransaction
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.MutableStateFlow

@Module
@TestInstallIn(
    components = [SingletonComponent::class, ActivityRetainedComponent::class],
    replaces = [DatabaseModule::class, DatabaseBindModule::class]
)
object FakeDatabaseModule {

    @Provides
    fun provideInMemoryDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    fun provideFakeDatabaseState(): MutableStateFlow<DatabaseState> {
        return MutableStateFlow(DatabaseState.OPEN)
    }

    @Provides
    fun provideFakeDatabaseRepository(
        appDatabase: AppDatabase,
        databaseState: MutableStateFlow<DatabaseState>
    ): DatabaseRepository {
        return FakeDatabaseRepositoryImpl(appDatabase, databaseState)
    }

    @Provides
    fun provideCheckDatabasePassphrase(): CheckDatabasePassphrase {
        return object : CheckDatabasePassphrase {
            override suspend fun invoke(passphrase: ByteArray): Result<Unit, OpenDatabaseError> {
                return Ok(Unit)
            }
        }
    }

    @Provides
    fun provideOpenAppDatabase(): OpenAppDatabase {
        return object : OpenAppDatabase {
            override suspend fun invoke(passphrase: ByteArray): Result<Unit, OpenDatabaseError> {
                return Ok(Unit)
            }
        }
    }

    @Provides
    fun provideDaoProvider(): DaoProvider {
        return FakeDaoProviderImpl()
    }

    @Provides
    fun provideChangeDatabasePassphrase(): ChangeDatabasePassphrase {
        return object : ChangeDatabasePassphrase {
            override suspend fun invoke(newPassphrase: ByteArray): Result<Unit, ChangeDatabasePassphraseError> {
                return Ok(Unit)
            }
        }
    }

    @Provides
    fun provideCloseAppDatabase(databaseRepository: DatabaseRepository): CloseAppDatabase {
        return object : CloseAppDatabase {
            override suspend fun invoke() {
                databaseRepository.close()
            }
        }
    }

    @Provides
    fun provideIsAppDatabaseOpen(databaseRepository: DatabaseRepository): IsAppDatabaseOpen {
        return object : IsAppDatabaseOpen {
            override fun invoke(): Boolean {
                return databaseRepository.isOpen()
            }
        }
    }

    @Provides
    fun provideCreateAppDatabase(databaseRepository: DatabaseRepository): CreateAppDatabase {
        return object : CreateAppDatabase {
            override suspend fun invoke(passphrase: ByteArray): Result<Unit, CreateDatabaseError> {
                databaseRepository.createDatabase(passphrase)
                return Ok(Unit)
            }
        }
    }

    @Provides
    fun providesRunInTransaction(databaseRepository: DatabaseRepository): RunInTransaction {
        return object : RunInTransaction {
            override suspend fun <V> invoke(block: suspend () -> V): V? {
                return databaseRepository.runInTransaction(block)
            }
        }
    }
}
