package ch.admin.foitt.wallet.feature.presentationRequest.di

import ch.admin.foitt.wallet.feature.presentationRequest.data.repository.PresentationRequestRepositoryImpl
import ch.admin.foitt.wallet.feature.presentationRequest.domain.repository.PresentationRequestRepository
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestCredentialListFlow
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestFlow
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.SubmitPresentation
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation.GetPresentationRequestCredentialListFlowImpl
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation.GetPresentationRequestFlowImpl
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation.SubmitPresentationImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
interface PresentationRequestModule {
    @Binds
    fun bindSubmitPresentation(
        useCase: SubmitPresentationImpl
    ): SubmitPresentation

    @Binds
    @ActivityRetainedScoped
    fun bindPresentationRequestRepository(
        repo: PresentationRequestRepositoryImpl
    ): PresentationRequestRepository

    @Binds
    fun bindGetPresentationRequestFlow(
        useCase: GetPresentationRequestFlowImpl
    ): GetPresentationRequestFlow

    @Binds
    fun bindGetPresentationRequestCredentialListFlow(
        useCase: GetPresentationRequestCredentialListFlowImpl
    ): GetPresentationRequestCredentialListFlow
}
