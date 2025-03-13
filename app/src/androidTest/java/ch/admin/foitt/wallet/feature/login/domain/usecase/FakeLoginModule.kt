package ch.admin.foitt.wallet.feature.login.domain.usecase

import ch.admin.foitt.wallet.platform.login.data.repository.DeviceLockoutStartRepository
import ch.admin.foitt.wallet.platform.login.data.repository.LoginAttemptsRepositoryImpl
import ch.admin.foitt.wallet.feature.login.di.LoginBindingsModule
import ch.admin.foitt.wallet.platform.login.domain.repository.LockoutStartRepository
import ch.admin.foitt.wallet.platform.login.domain.repository.LoginAttemptsRepository
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.GetLockoutDurationImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.GetRemainingLoginAttemptsImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.IncreaseFailedLoginAttemptsCounterImpl
import ch.admin.foitt.wallet.feature.login.domain.usecase.implementation.LockTriggerImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.ResetLockoutImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.GetLockoutDuration
import ch.admin.foitt.wallet.platform.login.domain.usecase.GetRemainingLoginAttempts
import ch.admin.foitt.wallet.platform.login.domain.usecase.IncreaseFailedLoginAttemptsCounter
import ch.admin.foitt.wallet.platform.login.domain.usecase.ResetLockout
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(components = [ActivityRetainedComponent::class], replaces = [LoginBindingsModule::class])
internal interface FakeLoginBindingsModule {
    @Binds
    fun bindUptimeRepository(
        repo: DeviceLockoutStartRepository
    ): LockoutStartRepository

    @Binds
    fun bindLoginAttemptsRepository(
        repo: LoginAttemptsRepositoryImpl
    ): LoginAttemptsRepository

    @Binds
    fun bindGetRemainingLoginAttempts(
        useCase: GetRemainingLoginAttemptsImpl
    ): GetRemainingLoginAttempts

    @Binds
    fun bindResetLoginLock(
        useCase: ResetLockoutImpl
    ): ResetLockout

    @Binds
    fun bindGetRemainingLockDuration(
        useCase: GetLockoutDurationImpl
    ): GetLockoutDuration

    @Binds
    fun bindHandleFailedLoginAttempt(
        useCase: IncreaseFailedLoginAttemptsCounterImpl
    ): IncreaseFailedLoginAttemptsCounter

    @Binds
    fun bindLockTriggerUseCase(
        useCase: LockTriggerImpl
    ): LockTrigger

    @Binds
    fun bindIsDevicePinSet(
        useCase: FakeIsDevicePinSet
    ): IsDevicePinSet
}
