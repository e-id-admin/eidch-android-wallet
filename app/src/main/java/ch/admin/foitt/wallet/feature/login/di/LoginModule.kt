package ch.admin.foitt.wallet.feature.login.di

import ch.admin.foitt.wallet.feature.login.data.repository.DeviceLockoutStartRepository
import ch.admin.foitt.wallet.feature.login.data.repository.LoginAttemptsRepositoryImpl
import ch.admin.foitt.wallet.feature.login.domain.repository.LockoutStartRepository
import ch.admin.foitt.wallet.feature.login.domain.repository.LoginAttemptsRepository
import ch.admin.foitt.wallet.feature.login.domain.usecase.GetLockoutDuration
import ch.admin.foitt.wallet.feature.login.domain.usecase.GetRemainingLoginAttempts
import ch.admin.foitt.wallet.feature.login.domain.usecase.IncreaseFailedLoginAttemptsCounter
import ch.admin.foitt.wallet.feature.login.domain.usecase.IsDevicePinSet
import ch.admin.foitt.wallet.feature.login.domain.usecase.LockTrigger
import ch.admin.foitt.wallet.feature.login.domain.usecase.ResetLockout
import ch.admin.foitt.wallet.feature.login.domain.usecase.implementation.GetLockoutDurationImpl
import ch.admin.foitt.wallet.feature.login.domain.usecase.implementation.GetRemainingLoginAttemptsImpl
import ch.admin.foitt.wallet.feature.login.domain.usecase.implementation.IncreaseFailedLoginAttemptsCounterImpl
import ch.admin.foitt.wallet.feature.login.domain.usecase.implementation.IsDevicePinSetImpl
import ch.admin.foitt.wallet.feature.login.domain.usecase.implementation.LockTriggerImpl
import ch.admin.foitt.wallet.feature.login.domain.usecase.implementation.ResetLockoutImpl
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
    fun bindUptimeRepository(
        repo: DeviceLockoutStartRepository
    ): LockoutStartRepository

    @Binds
    fun bindLoginAttemptsRepository(
        repo: LoginAttemptsRepositoryImpl
    ): LoginAttemptsRepository

    @Binds
    fun bindResetLoginLock(
        useCase: ResetLockoutImpl
    ): ResetLockout

    @Binds
    fun bindGetRemainingLockDuration(
        useCase: GetLockoutDurationImpl
    ): GetLockoutDuration

    @Binds
    fun bindIncreaseFailedLoginAttemptsCounter(
        useCase: IncreaseFailedLoginAttemptsCounterImpl
    ): IncreaseFailedLoginAttemptsCounter

    @Binds
    fun bindLockTriggerUseCase(
        useCase: LockTriggerImpl
    ): LockTrigger

    @Binds
    fun bindIsDevicePinSet(
        useCase: IsDevicePinSetImpl
    ): IsDevicePinSet

    @Binds
    fun bindGetRemainingLoginAttempts(
        useCase: GetRemainingLoginAttemptsImpl
    ): GetRemainingLoginAttempts
}
