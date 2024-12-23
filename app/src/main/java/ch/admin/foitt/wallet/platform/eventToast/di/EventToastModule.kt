package ch.admin.foitt.wallet.platform.eventToast.di

import ch.admin.foitt.wallet.platform.eventToast.data.repository.PassphraseChangeSuccessToastRepositoryImpl
import ch.admin.foitt.wallet.platform.eventToast.domain.repository.PassphraseChangeSuccessToastRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
class EventToastModule

@Module
@InstallIn(ActivityRetainedComponent::class)
interface EventToastBindingModule {
    @Binds
    @ActivityRetainedScoped
    fun bindSharedFlowRepo(
        repo: PassphraseChangeSuccessToastRepositoryImpl
    ): PassphraseChangeSuccessToastRepository
}
