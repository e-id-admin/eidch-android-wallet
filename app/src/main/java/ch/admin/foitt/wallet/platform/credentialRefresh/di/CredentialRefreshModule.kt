package ch.admin.foitt.wallet.platform.credentialRefresh.di

import ch.admin.foitt.wallet.platform.credentialRefresh.data.repository.CredentialRefreshDataRepositoryImpl
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.GetBindingKeyPair
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.IsCredentialRefreshable
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.implementation.GetBindingKeyPairImpl
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.implementation.IsCredentialRefreshableImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface CredentialRefreshModule {

    @Binds
    @ActivityRetainedScoped
    fun bindCredentialRefreshDataRepository(
        repo: CredentialRefreshDataRepositoryImpl
    ): CredentialRefreshDataRepository

    @Binds
    fun bindGetBindingKeyPair(
        useCase: GetBindingKeyPairImpl
    ): GetBindingKeyPair

    @Binds
    fun bindIsCredentialRefreshable(
        useCase: IsCredentialRefreshableImpl
    ): IsCredentialRefreshable
}
