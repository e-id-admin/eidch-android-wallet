package ch.admin.foitt.wallet.feature.credentialDetail.di

import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuerDisplaysFlow
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.implementation.GetCredentialIssuerDisplaysFlowImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
interface CredentialDetailModule {
    @Binds
    fun bindGetCredentialIssuerDisplaysFlow(
        useCase: GetCredentialIssuerDisplaysFlowImpl
    ): GetCredentialIssuerDisplaysFlow
}
