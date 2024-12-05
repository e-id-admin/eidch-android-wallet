package ch.admin.foitt.wallet.platform.crypto.di

import ch.admin.foitt.wallet.platform.crypto.domain.model.HashConfiguration
import ch.admin.foitt.wallet.platform.crypto.domain.usecase.HashDataWithSalt
import ch.admin.foitt.wallet.platform.crypto.domain.usecase.implementation.HashDataWithSaltImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class HashDataModule {
    @Provides
    fun provideHashAlgorithm(): HashConfiguration = HashConfiguration()
}

@Module
@InstallIn(SingletonComponent::class)
interface HashDataBindings {
    @Binds
    fun bindHashDataWithSalt(
        hashDataWithSaltImpl: HashDataWithSaltImpl,
    ): HashDataWithSalt
}
