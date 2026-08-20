package ch.admin.foitt.wallet.feature.credentialDetail.presentation.model

sealed interface BatchIssuanceInfoUiState {
    object Initial : BatchIssuanceInfoUiState

    sealed interface Ready : BatchIssuanceInfoUiState {
        val availableUsages: String
        val lastRenewal: String

        data class Normal(
            override val availableUsages: String,
            override val lastRenewal: String,
            val refreshThreshold: Int,
        ) : Ready

        data class Exhausted(
            override val availableUsages: String,
            override val lastRenewal: String,
        ) : Ready
    }
}

enum class BatchIssuanceToastEvent {
    RENEWAL_SUCCESS,
    RENEWAL_FAILURE
}
