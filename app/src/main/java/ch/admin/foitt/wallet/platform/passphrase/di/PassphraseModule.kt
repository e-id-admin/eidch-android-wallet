package ch.admin.foitt.wallet.platform.passphrase.di

import ch.admin.foitt.wallet.platform.passphrase.data.repository.PassphraseRepositoryImpl
import ch.admin.foitt.wallet.platform.passphrase.data.repository.PepperIvRepositoryImpl
import ch.admin.foitt.wallet.platform.passphrase.data.repository.SaltRepositoryImpl
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphrasePepperKeyConfig
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PassphraseStorageKeyConfig
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PassphraseRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PepperIvRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.SaltRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.DeleteSecretKey
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.EncryptAndSavePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.GetPassphraseWasDeleted
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.InitializePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.LoadAndDecryptPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.SavePassphraseWasDeleted
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.DeleteSecretKeyImpl
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.EncryptAndSavePassphraseImpl
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.GetPassphraseWasDeletedImpl
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.HashPassphraseImpl
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.InitializePassphraseImpl
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.LoadAndDecryptPassphraseImpl
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.PepperPassphraseImpl
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.SavePassphraseWasDeletedImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
internal class PassphraseModule {
    @Provides
    @ActivityRetainedScoped
    fun providePassphraseStorageConfig(): PassphraseStorageKeyConfig = PassphraseStorageKeyConfig()

    @Provides
    @ActivityRetainedScoped
    fun providePassphrasePepperConfig(): PassphrasePepperKeyConfig = PassphrasePepperKeyConfig()
}

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface PassphraseBindings {

    @Binds
    @ActivityRetainedScoped
    fun bindPassphraseRepository(
        repo: PassphraseRepositoryImpl
    ): PassphraseRepository

    @Binds
    fun bindEncryptAndSavePassphrase(
        useCase: EncryptAndSavePassphraseImpl
    ): EncryptAndSavePassphrase

    @Binds
    fun bindLoadAndDecryptPassphrase(
        useCase: LoadAndDecryptPassphraseImpl
    ): LoadAndDecryptPassphrase

    @Binds
    fun bindInitializePassphrase(useCase: InitializePassphraseImpl): InitializePassphrase

    @Binds
    fun bindDeleteSecretKey(
        useCase: DeleteSecretKeyImpl
    ): DeleteSecretKey

    @Binds
    fun bindSavePassphraseDidChange(useCase: SavePassphraseWasDeletedImpl): SavePassphraseWasDeleted

    @Binds
    fun bindGetPassphraseDidChange(useCase: GetPassphraseWasDeletedImpl): GetPassphraseWasDeleted

    @Binds
    @ActivityRetainedScoped
    fun bindSaltRepository(
        repo: SaltRepositoryImpl
    ): SaltRepository

    @Binds
    fun bindHashPassphrase(
        useCase: HashPassphraseImpl,
    ): HashPassphrase

    @Binds
    @ActivityRetainedScoped
    fun bindPepperIvRepository(
        repo: PepperIvRepositoryImpl
    ): PepperIvRepository

    @Binds
    fun bindPepperPassphrase(
        useCase: PepperPassphraseImpl
    ): PepperPassphrase
}
