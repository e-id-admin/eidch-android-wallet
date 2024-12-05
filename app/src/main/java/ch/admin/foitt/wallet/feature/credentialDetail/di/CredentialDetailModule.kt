package ch.admin.foitt.wallet.feature.credentialDetail.di

import ch.admin.foitt.wallet.feature.credentialDetail.data.repository.CredentialDetailRepositoryImpl
import ch.admin.foitt.wallet.feature.credentialDetail.domain.repository.CredentialDetailRepository
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialDetailFlow
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.implementation.GetCredentialDetailFlowImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
interface CredentialDetailModule {
    @Binds
    fun bindGetCredentialDetailFlow(
        useCase: GetCredentialDetailFlowImpl
    ): GetCredentialDetailFlow

    @Binds
    @ActivityRetainedScoped
    fun bindCredentialDetailRepository(
        repo: CredentialDetailRepositoryImpl
    ): CredentialDetailRepository
}
