package ch.admin.foitt.wallet.platform.biometrics.domain.model

import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.DeleteSecretKeyError

sealed interface ResetBiometricsError {
    data class Unexpected(val cause: Throwable?) : ResetBiometricsError
}

fun DeleteSecretKeyError.toResetBiometricsError(): ResetBiometricsError = when (this) {
    is DeleteSecretKeyError.Unexpected -> ResetBiometricsError.Unexpected(this.throwable)
}
