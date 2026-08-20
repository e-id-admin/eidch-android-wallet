package ch.admin.foitt.wallet.feature.credentialDetail.di

import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuanceType
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuerDisplaysFlow
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.HasExhaustedBatchCopies
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.implementation.GetCredentialIssuanceTypeImpl
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.implementation.GetCredentialIssuerDisplaysFlowImpl
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.implementation.HasExhaustedBatchCopiesImpl
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

    @Binds
    fun bindGetCredentialIssuanceType(
        useCase: GetCredentialIssuanceTypeImpl
    ): GetCredentialIssuanceType

    @Binds
    fun bindHasExhaustedBatchCopies(
        useCase: HasExhaustedBatchCopiesImpl
    ): HasExhaustedBatchCopies
}
