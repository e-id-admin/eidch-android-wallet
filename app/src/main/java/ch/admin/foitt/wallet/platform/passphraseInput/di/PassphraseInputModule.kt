package ch.admin.foitt.wallet.platform.passphraseInput.di

import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseConstraints
import ch.admin.foitt.wallet.platform.passphraseInput.domain.usecase.ValidatePassphrase
import ch.admin.foitt.wallet.platform.passphraseInput.domain.usecase.implementation.ValidatePassphraseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class PassphraseInputModule {

    @Provides
    fun providePassphraseConstrains(): PassphraseConstraints = PassphraseConstraints()
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface PassphraseInputBindings {

    @Binds
    fun bindValidatePassphrase(
        useCase: ValidatePassphraseImpl
    ): ValidatePassphrase
}
