package ch.admin.foitt.wallet.feature.credentialOffer.di

import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.GetCredentialOfferFlow
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.implementation.GetCredentialOfferFlowImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
interface CredentialOfferModule {
    @Binds
    fun bindGetCredentialOfferFlow(
        useCase: GetCredentialOfferFlowImpl
    ): GetCredentialOfferFlow
}
