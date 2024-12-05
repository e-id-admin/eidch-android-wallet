package ch.admin.foitt.wallet.platform.scaffold.domain.model

import androidx.annotation.StringRes

sealed interface TopBarState {
    data object Root : TopBarState
    data object SystemBarPadding : TopBarState
    data object None : TopBarState
    data object Empty : TopBarState

    data class DetailsWithCustomSettings(
        val onUp: () -> Unit,
        @StringRes
        val titleId: Int?,
        val onSettings: () -> Unit
    ) : TopBarState

    data class Details(
        val onUp: () -> Unit,
        @StringRes
        val titleId: Int?,
    ) : TopBarState

    data class Transparent(
        val onUp: () -> Unit,
        @StringRes
        val titleId: Int,
    ) : TopBarState
}
