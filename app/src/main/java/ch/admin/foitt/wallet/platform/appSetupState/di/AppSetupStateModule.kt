package ch.admin.foitt.wallet.platform.appSetupState.di

import ch.admin.foitt.wallet.platform.appSetupState.data.repository.FirstCredentialAddedRepositoryImpl
import ch.admin.foitt.wallet.platform.appSetupState.data.repository.OnboardingStateCompletionRepository
import ch.admin.foitt.wallet.platform.appSetupState.data.repository.UseBiometricLoginRepositoryImpl
import ch.admin.foitt.wallet.platform.appSetupState.domain.implementation.GetFirstCredentialWasAddedImpl
import ch.admin.foitt.wallet.platform.appSetupState.domain.implementation.SaveFirstCredentialWasAddedImpl
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.FirstCredentialAddedRepository
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.OnboardingStateRepository
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.UseBiometricLoginRepository
import ch.admin.foitt.wallet.platform.appSetupState.domain.usecase.GetFirstCredentialWasAdded
import ch.admin.foitt.wallet.platform.appSetupState.domain.usecase.SaveFirstCredentialWasAdded
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AppSetupStateModule {

    @Binds
    fun bindEncryptedOnboardingStateRepository(
        repo: OnboardingStateCompletionRepository
    ): OnboardingStateRepository

    @Binds
    fun bindEncryptedUseBiometricLoginStateRepository(
        repo: UseBiometricLoginRepositoryImpl
    ): UseBiometricLoginRepository

    @Binds
    fun bindFirstCredentialAddedRepository(
        repo: FirstCredentialAddedRepositoryImpl
    ): FirstCredentialAddedRepository

    @Binds
    fun bindGetFirstCredentialWasAdded(
        useCase: GetFirstCredentialWasAddedImpl
    ): GetFirstCredentialWasAdded

    @Binds
    fun bindSaveFirstCredentialWasAdded(
        useCase: SaveFirstCredentialWasAddedImpl
    ): SaveFirstCredentialWasAdded
}
