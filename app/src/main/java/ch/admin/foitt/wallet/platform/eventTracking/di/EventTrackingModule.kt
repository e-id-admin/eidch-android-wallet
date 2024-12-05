package ch.admin.foitt.wallet.platform.eventTracking.di

import ch.admin.foitt.wallet.platform.eventTracking.data.repository.DynatraceUserPrivacyPolicyRepository
import ch.admin.foitt.wallet.platform.eventTracking.domain.repository.UserPrivacyPolicyRepository
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.ApplyUserPrivacyPolicy
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.IsUserPrivacyPolicyAcceptedFlow
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.ReportError
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.implementation.ApplyUserPrivacyPolicyImpl
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.implementation.IsUserPrivacyPolicyAcceptedFlowImpl
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.implementation.ReportErrorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface EventTrackingModuleSingleton {
    @Binds
    fun bindReportError(
        useCase: ReportErrorImpl,
    ): ReportError
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface EventTrackingModule {
    @Binds
    fun bindApplyUserPrivacyPolicy(
        useCase: ApplyUserPrivacyPolicyImpl,
    ): ApplyUserPrivacyPolicy

    @Binds
    fun bindIsUserPrivacyPolicyAcceptedFlow(
        useCase: IsUserPrivacyPolicyAcceptedFlowImpl,
    ): IsUserPrivacyPolicyAcceptedFlow

    @Binds
    @ActivityRetainedScoped
    fun bindUserPrivacyPolicyRepository(
        useCase: DynatraceUserPrivacyPolicyRepository,
    ): UserPrivacyPolicyRepository
}
