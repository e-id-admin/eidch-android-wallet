package ch.admin.foitt.wallet.platform.scaffold.domain.model

import androidx.annotation.StringRes

sealed interface TopBarState {
    data object None : TopBarState
    data object Empty : TopBarState

    data class DetailsWithCloseButton(
        val onUp: () -> Unit,
        @StringRes
        val titleId: Int?,
        val onClose: () -> Unit,
    ) : TopBarState

    data class Details(
        val onUp: () -> Unit,
        @StringRes
        val titleId: Int?,
    ) : TopBarState

    data class EmptyWithCloseButton(
        val onClose: () -> Unit,
    ) : TopBarState

    data class OnGradient(
        val onUp: () -> Unit,
        @StringRes
        val titleId: Int,
    ) : TopBarState
}
