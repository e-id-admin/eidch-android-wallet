package ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase

import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricManagerResult

interface BiometricsStatus {
    operator fun invoke(): BiometricManagerResult
}
