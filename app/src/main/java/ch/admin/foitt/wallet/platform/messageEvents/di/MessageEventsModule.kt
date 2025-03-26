package ch.admin.foitt.wallet.platform.messageEvents.di

import ch.admin.foitt.wallet.platform.messageEvents.data.repository.CredentialOfferEventRepositoryImpl
import ch.admin.foitt.wallet.platform.messageEvents.data.repository.PassphraseChangeEventRepositoryImpl
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialOfferEventRepository
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.PassphraseChangeEventRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
class MessageEventsModule

@Module
@InstallIn(ActivityRetainedComponent::class)
interface MessageEventsBindingModule {
    @Binds
    @ActivityRetainedScoped
    fun bindPassphraseChangeEventRepo(
        repo: PassphraseChangeEventRepositoryImpl
    ): PassphraseChangeEventRepository

    @Binds
    @ActivityRetainedScoped
    fun bindCredentialReceivedEventRepo(
        repo: CredentialOfferEventRepositoryImpl
    ): CredentialOfferEventRepository
}
