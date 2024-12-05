package ch.admin.foitt.wallet.feature.onboarding.di

import ch.admin.foitt.wallet.feature.onboarding.domain.usecase.SaveOnboardingState
import ch.admin.foitt.wallet.feature.onboarding.domain.usecase.implementation.SaveOnboardingStateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
interface OnboardingModule {
    @Binds
    fun bindSaveOnboardingState(
        useCase: SaveOnboardingStateImpl
    ): SaveOnboardingState
}
