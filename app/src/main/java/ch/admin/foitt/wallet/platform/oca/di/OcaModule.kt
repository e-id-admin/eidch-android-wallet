package ch.admin.foitt.wallet.platform.oca.di

import ch.admin.foitt.wallet.platform.oca.data.OcaRepositoryImpl
import ch.admin.foitt.wallet.platform.oca.domain.repository.OcaRepository
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchOcaBundleByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.FetchOcaBundleByFormatImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface OcaModule {
    @Binds
    @ActivityRetainedScoped
    fun bindOcaRepository(
        repo: OcaRepositoryImpl
    ): OcaRepository

    @Binds
    fun bindFetchOcaBundleByFormat(
        useCase: FetchOcaBundleByFormatImpl
    ): FetchOcaBundleByFormat
}
