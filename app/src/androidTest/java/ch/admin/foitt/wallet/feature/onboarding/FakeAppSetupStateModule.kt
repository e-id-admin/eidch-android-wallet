package ch.admin.foitt.wallet.feature.onboarding

import ch.admin.foitt.wallet.platform.appSetupState.data.repository.FirstCredentialAddedRepositoryImpl
import ch.admin.foitt.wallet.platform.appSetupState.data.repository.UseBiometricLoginRepositoryImpl
import ch.admin.foitt.wallet.platform.appSetupState.di.AppSetupStateModule
import ch.admin.foitt.wallet.platform.appSetupState.domain.implementation.GetFirstCredentialWasAddedImpl
import ch.admin.foitt.wallet.platform.appSetupState.domain.implementation.SaveFirstCredentialWasAddedImpl
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.FirstCredentialAddedRepository
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.OnboardingStateRepository
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.UseBiometricLoginRepository
import ch.admin.foitt.wallet.platform.appSetupState.domain.usecase.GetFirstCredentialWasAdded
import ch.admin.foitt.wallet.platform.appSetupState.domain.usecase.SaveFirstCredentialWasAdded
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [AppSetupStateModule::class])
interface FakeAppSetupStateModule {

    @Binds
    fun bindEncryptedOnboardingStateRepository(
        repo: FakeOnboardingStateRepository
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
