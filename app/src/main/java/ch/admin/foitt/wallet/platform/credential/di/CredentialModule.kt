package ch.admin.foitt.wallet.platform.credential.di

import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.IsBetaIssuer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.FetchCredentialImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.GetAnyCredentialImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.GetAnyCredentialsImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.IsBetaIssuerImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.MapToCredentialDisplayDataImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.SaveCredentialImpl
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.implementation.GetCredentialCardStateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface CredentialModule {

    @Binds
    fun bindFetchCredential(
        useCase: FetchCredentialImpl
    ): FetchCredential

    @Binds
    fun bindSaveCredential(
        useCase: SaveCredentialImpl
    ): SaveCredential

    @Binds
    fun bindGetAnyCredential(
        useCase: GetAnyCredentialImpl
    ): GetAnyCredential

    @Binds
    fun bindGetAnyCredentials(
        useCase: GetAnyCredentialsImpl
    ): GetAnyCredentials

    @Binds
    fun bindGetCredentialState(
        adapter: GetCredentialCardStateImpl
    ): GetCredentialCardState

    @Binds
    fun bindIsBetaIssuer(
        useCase: IsBetaIssuerImpl
    ): IsBetaIssuer

    @Binds
    fun bindMapToCredentialDisplayData(
        useCase: MapToCredentialDisplayDataImpl
    ): MapToCredentialDisplayData
}
