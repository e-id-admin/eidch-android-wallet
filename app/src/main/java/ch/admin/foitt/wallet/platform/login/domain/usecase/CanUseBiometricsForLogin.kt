package ch.admin.foitt.wallet.platform.login.domain.usecase

import ch.admin.foitt.wallet.platform.login.domain.model.CanUseBiometricsForLoginResult

interface CanUseBiometricsForLogin {
    suspend operator fun invoke(): CanUseBiometricsForLoginResult
}
