package ch.admin.foitt.wallet.platform.authenticateWithPassphrase.di

import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.usecase.AuthenticateWithPassphrase
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.usecase.implementation.AuthenticateWithPassphraseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
class AuthenticateWithPassphraseModule

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface AuthenticateWithPassphraseBindingsModule {
    @Binds
    fun bindAuthenticateWithPassphrase(
        useCase: AuthenticateWithPassphraseImpl,
    ): AuthenticateWithPassphrase
}
