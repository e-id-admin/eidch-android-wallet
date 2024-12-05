package ch.admin.foitt.wallet.feature.sessionTimeout.di

import ch.admin.foitt.wallet.feature.sessionTimeout.domain.SessionTimeoutNavigation
import ch.admin.foitt.wallet.feature.sessionTimeout.domain.UserInteractionFlow
import ch.admin.foitt.wallet.feature.sessionTimeout.domain.implementation.SessionTimeoutNavigationImpl
import ch.admin.foitt.wallet.feature.sessionTimeout.domain.implementation.UserInteractionFlowImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
class SessionTimeoutModule

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface SessionTimeoutBindingsModule {
    @Binds
    fun bindSessionTimeoutNavigation(
        useCase: SessionTimeoutNavigationImpl
    ): SessionTimeoutNavigation

    @Binds
    fun bindUserInteractionFlow(
        useCase: UserInteractionFlowImpl
    ): UserInteractionFlow
}
