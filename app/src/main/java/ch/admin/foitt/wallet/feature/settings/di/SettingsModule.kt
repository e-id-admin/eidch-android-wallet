package ch.admin.foitt.wallet.feature.settings.di

import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.StorePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.StorePassphraseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
class SettingsModule

@Module
@InstallIn(ActivityRetainedComponent::class)
interface SettingsBindingModule {
    @Binds
    fun bindStorePassphraseUseCase(
        useCase: StorePassphraseImpl
    ): StorePassphrase
}
