package ch.admin.foitt.wallet.feature.home.di

import ch.admin.foitt.wallet.feature.home.data.repository.HomeRepositoryImpl
import ch.admin.foitt.wallet.feature.home.domain.repository.HomeRepository
import ch.admin.foitt.wallet.feature.home.domain.usecase.GetHomeDataFlow
import ch.admin.foitt.wallet.feature.home.domain.usecase.implementation.GetHomeDataFlowImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
interface HomeModule {
    @Binds
    @ActivityRetainedScoped
    fun bindHomeRepository(
        repo: HomeRepositoryImpl
    ): HomeRepository

    @Binds
    fun bindGetHomeDataFlow(
        useCase: GetHomeDataFlowImpl
    ): GetHomeDataFlow
}
