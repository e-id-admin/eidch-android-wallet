package ch.admin.foitt.wallet.platform.ssi.di

import ch.admin.foitt.wallet.platform.ssi.data.repository.CredentialClaimDisplayRepoImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.CredentialClaimRepoImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.CredentialIssuerDisplayRepoImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.CredentialOfferRepositoryImpl
import ch.admin.foitt.wallet.platform.ssi.data.repository.CredentialRepoImpl
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialClaimDisplayRepo
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialClaimRepo
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialIssuerDisplayRepo
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteCredential
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialClaimDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialClaims
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialIssuerDisplayFlow
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.DeleteCredentialImpl
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.GetCredentialClaimDataImpl
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.GetCredentialClaimDisplaysImpl
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.GetCredentialClaimsImpl
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.GetCredentialIssuerDisplayFlowImpl
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.MapToCredentialClaimDataImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
interface SsiModule {

    @Binds
    fun bindCredentialClaimDisplayRepo(
        useCase: CredentialClaimDisplayRepoImpl
    ): CredentialClaimDisplayRepo

    @Binds
    fun bindCredentialClaimRepo(
        useCase: CredentialClaimRepoImpl
    ): CredentialClaimRepo

    @Binds
    fun bindCredentialRepo(
        useCase: CredentialRepoImpl
    ): CredentialRepo

    @Binds
    fun bindDeleteCredential(
        useCase: DeleteCredentialImpl
    ): DeleteCredential

    @Binds
    fun bindGetCredentialClaims(
        useCase: GetCredentialClaimsImpl
    ): GetCredentialClaims

    @Binds
    fun bindGetCredentialClaimDisplays(
        useCase: GetCredentialClaimDisplaysImpl
    ): GetCredentialClaimDisplays

    @Binds
    fun bindGetCredentialClaimData(
        useCase: GetCredentialClaimDataImpl
    ): GetCredentialClaimData

    @Binds
    fun bindMapToCredentialClaimData(
        useCase: MapToCredentialClaimDataImpl
    ): MapToCredentialClaimData

    @Binds
    @ActivityRetainedScoped
    fun bindCredentialOfferRepository(
        repo: CredentialOfferRepositoryImpl
    ): CredentialOfferRepository

    @Binds
    @ActivityRetainedScoped
    fun bindCredentialIssuerDisplayRepository(
        repo: CredentialIssuerDisplayRepoImpl
    ): CredentialIssuerDisplayRepo

    @Binds
    fun bindGetCredentialIssuerDisplayFlow(
        useCase: GetCredentialIssuerDisplayFlowImpl
    ): GetCredentialIssuerDisplayFlow
}
