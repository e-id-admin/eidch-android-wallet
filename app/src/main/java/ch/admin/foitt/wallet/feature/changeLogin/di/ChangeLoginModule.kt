package ch.admin.foitt.wallet.feature.changeLogin.di

import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.ChangePassphrase
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation.ChangePassphraseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
interface ChangeLoginModule {
    @Binds
    fun bindChangePassphrase(useCase: ChangePassphraseImpl): ChangePassphrase
}
