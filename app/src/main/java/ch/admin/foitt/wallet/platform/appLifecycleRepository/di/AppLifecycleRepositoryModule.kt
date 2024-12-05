package ch.admin.foitt.wallet.platform.appLifecycleRepository.di

import ch.admin.foitt.wallet.platform.appLifecycleRepository.data.repository.AppLifecycleRepositoryImpl
import ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.repository.AppLifecycleRepository
import ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.usecase.GetAppLifecycleState
import ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.usecase.implementation.GetAppLifecycleStateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppLifecycleRepositoryModule {

    @Singleton
    @Binds
    fun bindAppLifecycleRepository(
        provider: AppLifecycleRepositoryImpl
    ): AppLifecycleRepository

    @Binds
    fun bindGetAppLifecycleState(
        useCase: GetAppLifecycleStateImpl
    ): GetAppLifecycleState
}
