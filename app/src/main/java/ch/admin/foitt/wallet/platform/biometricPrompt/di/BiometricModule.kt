package ch.admin.foitt.wallet.platform.biometricPrompt.di

import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.BiometricsStatus
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.GetAuthenticators
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.LaunchBiometricPrompt
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.implementation.BiometricsStatusImpl
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.implementation.GetAuthenticatorsImpl
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.implementation.LaunchBiometricPromptImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
interface BiometricModule {

    @Binds
    fun bindBiometricAuthenticationAvailable(
        useCase: BiometricsStatusImpl
    ): BiometricsStatus

    @Binds
    fun bindGetAuthenticators(
        useCase: GetAuthenticatorsImpl
    ): GetAuthenticators

    @Binds
    fun bindLaunchBiometricPrompt(
        useCase: LaunchBiometricPromptImpl
    ): LaunchBiometricPrompt
}
