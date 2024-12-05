package ch.admin.foitt.wallet.platform.keystoreCrypto.di

import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetCipherForDecryption
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetCipherForEncryption
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetOrCreateSecretKey
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.implementation.GetCipherForDecryptionImpl
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.implementation.GetCipherForEncryptionImpl
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.implementation.GetOrCreateSecretKeyImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface KeystoreCryptoModule {
    @Binds
    fun bindGetCipherForEncryption(
        useCase: GetCipherForEncryptionImpl
    ): GetCipherForEncryption

    @Binds
    fun bindGetCipherForDecryption(
        useCase: GetCipherForDecryptionImpl
    ): GetCipherForDecryption

    @Binds
    fun bindGetOrCreateSecretKey(
        useCase: GetOrCreateSecretKeyImpl
    ): GetOrCreateSecretKey
}
