package ch.admin.foitt.wallet.platform.database.di

import ch.admin.foitt.wallet.platform.database.data.DatabaseInitializer
import ch.admin.foitt.wallet.platform.database.data.DatabaseWrapper
import ch.admin.foitt.wallet.platform.database.data.SqlCipherDatabaseInitializer
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.database.domain.usecase.ChangeDatabasePassphrase
import ch.admin.foitt.wallet.platform.database.domain.usecase.CheckDatabasePassphrase
import ch.admin.foitt.wallet.platform.database.domain.usecase.CloseAppDatabase
import ch.admin.foitt.wallet.platform.database.domain.usecase.CreateAppDatabase
import ch.admin.foitt.wallet.platform.database.domain.usecase.IsAppDatabaseOpen
import ch.admin.foitt.wallet.platform.database.domain.usecase.OpenAppDatabase
import ch.admin.foitt.wallet.platform.database.domain.usecase.RunInTransaction
import ch.admin.foitt.wallet.platform.database.domain.usecase.implementation.ChangeDatabasePassphraseImpl
import ch.admin.foitt.wallet.platform.database.domain.usecase.implementation.CheckDatabasePassphraseImpl
import ch.admin.foitt.wallet.platform.database.domain.usecase.implementation.CloseAppDatabaseImpl
import ch.admin.foitt.wallet.platform.database.domain.usecase.implementation.CreateAppDatabaseImpl
import ch.admin.foitt.wallet.platform.database.domain.usecase.implementation.IsAppDatabaseOpenImpl
import ch.admin.foitt.wallet.platform.database.domain.usecase.implementation.OpenAppDatabaseImpl
import ch.admin.foitt.wallet.platform.database.domain.usecase.implementation.RunInTransactionImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface DatabaseModule {
    @Binds
    fun bindDatabaseRepository(repository: DatabaseWrapper): DatabaseRepository

    @Binds
    fun bindDaoProvider(provider: DatabaseWrapper): DaoProvider

    @Binds
    fun bindDatabaseInitializer(initializer: SqlCipherDatabaseInitializer): DatabaseInitializer
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface DatabaseBindModule {

    @Binds
    fun bindCreateDatabase(useCase: CreateAppDatabaseImpl): CreateAppDatabase

    @Binds
    fun bindOpenDatabase(useCase: OpenAppDatabaseImpl): OpenAppDatabase

    @Binds
    fun bindCloseDatabase(useCase: CloseAppDatabaseImpl): CloseAppDatabase

    @Binds
    fun bindCheckDatabasePassphrase(useCase: CheckDatabasePassphraseImpl): CheckDatabasePassphrase

    @Binds
    fun bindChangeDatabasePassphrase(useCase: ChangeDatabasePassphraseImpl): ChangeDatabasePassphrase

    @Binds
    fun bindIsAppDatabaseOpen(useCase: IsAppDatabaseOpenImpl): IsAppDatabaseOpen

    @Binds
    fun runInTransaction(useCase: RunInTransactionImpl): RunInTransaction
}
