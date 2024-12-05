package ch.admin.foitt.wallet.platform.userInteraction.di

import ch.admin.foitt.wallet.platform.userInteraction.data.repository.UserInteractionRepositoryImpl
import ch.admin.foitt.wallet.platform.userInteraction.domain.repository.UserInteractionRepository
import ch.admin.foitt.wallet.platform.userInteraction.domain.usecase.UserInteraction
import ch.admin.foitt.wallet.platform.userInteraction.domain.usecase.implementation.UserInteractionImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
class UserInteractionModule

@Module
@InstallIn(ActivityRetainedComponent::class)
interface UserInteractionBindingsModule {
    @ActivityRetainedScoped
    @Binds
    fun bindUserInteractionRepository(
        provider: UserInteractionRepositoryImpl
    ): UserInteractionRepository

    @Binds
    fun bindUserInteraction(
        useCase: UserInteractionImpl
    ): UserInteraction
}
