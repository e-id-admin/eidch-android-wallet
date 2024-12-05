package ch.admin.foitt.wallet.platform.biometrics.di

import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.GetBiometricsCipher
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.InitializeCipherWithBiometrics
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.SaveUseBiometricLogin
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.implementation.GetBiometricsCipherImpl
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.implementation.InitializeCipherWithBiometricsImpl
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.implementation.ResetBiometricsImpl
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.implementation.SaveUseBiometricLoginImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
class AuthenticationModule

@Module
@InstallIn(ActivityRetainedComponent::class)
interface AuthenticationBindingsModule {
    @Binds
    fun bindInitializeCipherWithBiometrics(
        useCase: InitializeCipherWithBiometricsImpl
    ): InitializeCipherWithBiometrics

    @Binds
    fun bindResetBiometricsUseCase(
        useCase: ResetBiometricsImpl
    ): ResetBiometrics

    @Binds
    fun bindGetBiometricsCipher(
        useCase: GetBiometricsCipherImpl
    ): GetBiometricsCipher

    @Binds
    fun bindSaveUseBiometricLogin(
        useCase: SaveUseBiometricLoginImpl
    ): SaveUseBiometricLogin
}
