package ch.admin.foitt.wallet.feature.login.domain.usecase

import javax.inject.Inject

class FakeIsDevicePinSet @Inject constructor() : IsDevicePinSet {
    override fun invoke(): Boolean = true
}
