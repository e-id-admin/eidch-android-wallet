package ch.admin.foitt.wallet.platform.payloadEncryption.di

import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.CreatePayloadEncryptionKeyPair
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryption
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.implementation.CreatePayloadEncryptionKeyPairImpl
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.implementation.GetPayloadEncryptionImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface PayloadEncryptionModule {
    @Binds
    fun bindCreatePayloadEncryptionKeyPair(
        useCase: CreatePayloadEncryptionKeyPairImpl
    ): CreatePayloadEncryptionKeyPair

    @Binds
    fun bindGetPayloadEncryption(
        useCase: GetPayloadEncryptionImpl
    ): GetPayloadEncryption
}
