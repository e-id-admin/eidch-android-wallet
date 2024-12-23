package ch.admin.foitt.wallet.platform.actorMetadata.di

import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchIssuerDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchVerifierDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.FetchIssuerDisplayDataImpl
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.FetchVerifierDisplayDataImpl
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.implementation.GetActorUiStateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface ActorMetadataModule {
    @Binds
    fun bindFetchIssuerDisplayData(
        useCase: FetchIssuerDisplayDataImpl
    ): FetchIssuerDisplayData

    @Binds
    fun bindFetchVerifierDisplayData(
        useCase: FetchVerifierDisplayDataImpl
    ): FetchVerifierDisplayData

    @Binds
    fun bindGetActorUiState(
        adapter: GetActorUiStateImpl
    ): GetActorUiState
}
