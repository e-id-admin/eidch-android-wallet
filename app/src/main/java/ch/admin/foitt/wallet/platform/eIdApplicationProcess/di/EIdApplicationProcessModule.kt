package ch.admin.foitt.wallet.platform.eIdApplicationProcess.di

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.EIdRequestCaseRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository.EIdRequestStateRepositoryImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SaveEIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SaveEIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.SaveEIdRequestCaseImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.SaveEIdRequestStateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
interface EIdApplicationProcessModule {

    @Binds
    @ActivityRetainedScoped
    fun bindEIdRequestCaseRepository(
        repo: EIdRequestCaseRepositoryImpl
    ): EIdRequestCaseRepository

    @Binds
    @ActivityRetainedScoped
    fun bindEIdRequestStateRepository(
        repo: EIdRequestStateRepositoryImpl
    ): EIdRequestStateRepository

    @Binds
    fun bindSaveEIdRequestCase(
        useCase: SaveEIdRequestCaseImpl
    ): SaveEIdRequestCase

    @Binds
    fun bindSaveEIdRequestState(
        useCase: SaveEIdRequestStateImpl
    ): SaveEIdRequestState
}
