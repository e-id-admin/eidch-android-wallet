package ch.admin.foitt.wallet.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object WalletTopBarColors {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun default() = TopAppBarDefaults.topAppBarColors(
        containerColor = WalletTheme.colorScheme.surface,
        scrolledContainerColor = WalletTheme.colorScheme.surface.copy(alpha = 0.85f),
        navigationIconContentColor = Color.Unspecified,
        titleContentColor = Color.Unspecified,
        actionIconContentColor = Color.Unspecified,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun transparent() = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
        navigationIconContentColor = Color.Unspecified,
        titleContentColor = Color.Unspecified,
        actionIconContentColor = Color.Unspecified,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun transparentFixed() = transparent().copy(
        navigationIconContentColor = WalletTheme.colorScheme.onPrimaryFixed,
        titleContentColor = WalletTheme.colorScheme.onPrimaryFixed,
        actionIconContentColor = WalletTheme.colorScheme.onPrimaryFixed,
    )
}
