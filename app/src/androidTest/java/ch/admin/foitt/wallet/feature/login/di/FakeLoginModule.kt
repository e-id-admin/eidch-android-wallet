package ch.admin.foitt.wallet.feature.login.di

import ch.admin.foitt.wallet.feature.login.domain.usecase.FakeIsDeviceSecureLockScreenConfigured
import ch.admin.foitt.wallet.feature.login.domain.usecase.IsDeviceSecureLockScreenConfigured
import ch.admin.foitt.wallet.feature.login.domain.usecase.LockTrigger
import ch.admin.foitt.wallet.feature.login.domain.usecase.implementation.LockTriggerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(components = [ActivityRetainedComponent::class], replaces = [LoginBindingsModule::class])
internal interface FakeLoginBindingsModule {
    @Binds
    fun bindLockTriggerUseCase(
        useCase: LockTriggerImpl
    ): LockTrigger

    @Binds
    fun bindIsDeviceSecureLockScreenConfigured(
        useCase: FakeIsDeviceSecureLockScreenConfigured
    ): IsDeviceSecureLockScreenConfigured
}
