package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.wallet.R

enum class TrustStatus {
    TRUSTED,
    NOT_TRUSTED,
    TRUSTED_PROXIMITY_VERIFIER,
    NOT_TRUSTED_PROXIMITY_VERIFIER,
    EXTERNAL,
    UNKNOWN,
}

fun TrustStatus.toIcon(): Int? {
    return when (this) {
        TrustStatus.TRUSTED_PROXIMITY_VERIFIER,
        TrustStatus.TRUSTED -> R.drawable.wallet_ic_trusted
        TrustStatus.NOT_TRUSTED,
        TrustStatus.EXTERNAL,
        TrustStatus.NOT_TRUSTED_PROXIMITY_VERIFIER,
        TrustStatus.UNKNOWN -> null
    }
}
