package ch.admin.foitt.wallet.platform.biometrics.domain.usecase

import ch.admin.foitt.wallet.platform.biometrics.domain.model.ResetBiometricsError
import com.github.michaelbull.result.Result

interface ResetBiometrics {
    suspend operator fun invoke(): Result<Unit, ResetBiometricsError>
}
