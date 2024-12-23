package ch.admin.foitt.wallet.platform.trustRegistry.di

import ch.admin.foitt.wallet.platform.trustRegistry.data.TrustStatementRepositoryImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchTrustStatementFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.FetchTrustStatementFromDidImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.GetTrustUrlFromDidImpl
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateTrustStatementImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface TrustRegistryModule {
    @Binds
    fun bindGetTrustUrlFromDid(
        useCase: GetTrustUrlFromDidImpl
    ): GetTrustUrlFromDid

    @Binds
    fun bindValidateTrustStatement(
        useCase: ValidateTrustStatementImpl
    ): ValidateTrustStatement

    @Binds
    @ActivityRetainedScoped
    fun bindTrustStatementRepository(
        repo: TrustStatementRepositoryImpl
    ): TrustStatementRepository

    @Binds
    fun bindFetchTrustStatementFromDid(
        useCase: FetchTrustStatementFromDidImpl
    ): FetchTrustStatementFromDid
}
