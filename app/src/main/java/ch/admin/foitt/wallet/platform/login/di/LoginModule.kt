package ch.admin.foitt.wallet.platform.login.di

import ch.admin.foitt.wallet.platform.login.data.repository.DeviceLockoutStartRepository
import ch.admin.foitt.wallet.platform.login.data.repository.LoginAttemptsRepositoryImpl
import ch.admin.foitt.wallet.platform.login.domain.repository.LockoutStartRepository
import ch.admin.foitt.wallet.platform.login.domain.repository.LoginAttemptsRepository
import ch.admin.foitt.wallet.platform.login.domain.usecase.AfterLoginWork
import ch.admin.foitt.wallet.platform.login.domain.usecase.BiometricLoginEnabled
import ch.admin.foitt.wallet.platform.login.domain.usecase.CanUseBiometricsForLogin
import ch.admin.foitt.wallet.platform.login.domain.usecase.GetLockoutDuration
import ch.admin.foitt.wallet.platform.login.domain.usecase.GetRemainingLoginAttempts
import ch.admin.foitt.wallet.platform.login.domain.usecase.IncreaseFailedLoginAttemptsCounter
import ch.admin.foitt.wallet.platform.login.domain.usecase.LoginWithBiometrics
import ch.admin.foitt.wallet.platform.login.domain.usecase.LoginWithPassphrase
import ch.admin.foitt.wallet.platform.login.domain.usecase.NavigateToLogin
import ch.admin.foitt.wallet.platform.login.domain.usecase.ResetLockout
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.AfterLoginWorkImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.BiometricLoginEnabledImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.CanUseBiometricsForLoginImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.GetLockoutDurationImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.GetRemainingLoginAttemptsImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.IncreaseFailedLoginAttemptsCounterImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.LoginWithBiometricsImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.LoginWithPassphraseImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.NavigateToLoginImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.ResetLockoutImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
class LoginModule

@Module
@InstallIn(ActivityRetainedComponent::class)
internal interface LoginBindingsModule {
    @Binds
    fun bindLoginWithPassphrase(
        useCase: LoginWithPassphraseImpl,
    ): LoginWithPassphrase

    @Binds
    fun bindBiometricLoginEnabled(
        useCase: BiometricLoginEnabledImpl,
    ): BiometricLoginEnabled

    @Binds
    fun bindNavigateToLogin(
        useCase: NavigateToLoginImpl
    ): NavigateToLogin

    @Binds
    fun bindLoginWithBiometric(
        useCase: LoginWithBiometricsImpl
    ): LoginWithBiometrics

    @Binds
    fun bindCanUseBiometricsForLogin(
        useCase: CanUseBiometricsForLoginImpl
    ): CanUseBiometricsForLogin

    @Binds
    fun bindAfterLoginWork(
        useCase: AfterLoginWorkImpl
    ): AfterLoginWork

    @Binds
    fun bindDeviceLockoutStartRepository(
        repo: DeviceLockoutStartRepository
    ): LockoutStartRepository

    @Binds
    fun bindLoginAttemptsRepository(
        repo: LoginAttemptsRepositoryImpl
    ): LoginAttemptsRepository

    @Binds
    fun bindResetLockout(
        useCase: ResetLockoutImpl
    ): ResetLockout

    @Binds
    fun bindGetLockoutDuration(
        useCase: GetLockoutDurationImpl
    ): GetLockoutDuration

    @Binds
    fun bindIncreaseFailedLoginAttemptsCounter(
        useCase: IncreaseFailedLoginAttemptsCounterImpl
    ): IncreaseFailedLoginAttemptsCounter

    @Binds
    fun bindGetRemainingLoginAttempts(
        useCase: GetRemainingLoginAttemptsImpl
    ): GetRemainingLoginAttempts
}
