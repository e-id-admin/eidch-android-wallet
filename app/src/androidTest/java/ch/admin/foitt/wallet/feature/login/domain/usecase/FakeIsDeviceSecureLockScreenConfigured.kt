package ch.admin.foitt.wallet.feature.login.domain.usecase

import javax.inject.Inject

class FakeIsDeviceSecureLockScreenConfigured @Inject constructor() : IsDeviceSecureLockScreenConfigured {
    override fun invoke(): Boolean = true
}
