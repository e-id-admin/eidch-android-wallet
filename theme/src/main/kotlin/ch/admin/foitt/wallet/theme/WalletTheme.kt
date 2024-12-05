package ch.admin.foitt.wallet.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

@Composable
fun WalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val walletColorScheme = when {
        darkTheme -> WalletColorScheme.dark
        else -> WalletColorScheme.light
    }
    val walletShapes = WalletShapes.default
    val walletTypography = WalletTypography.default

    CompositionLocalProvider(
        LocalWalletColors provides walletColorScheme,
        LocalWalletTypography provides walletTypography,
        LocalWalletShapes provides walletShapes,
        LocalIsInDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = walletColorScheme.materialColorScheme,
            shapes = walletShapes.materialShapes,
            typography = walletTypography.materialTypography,
        ) {
            content()
        }
    }
}

val LocalWalletColors = staticCompositionLocalOf<WalletColorScheme> {
    error("No WalletColors defined")
}

val LocalWalletTypography = staticCompositionLocalOf<WalletTypography> {
    error("No WalletTypography defined")
}

val LocalWalletShapes = staticCompositionLocalOf<WalletShapes> {
    error("No WalletShapes defined")
}

val LocalIsInDarkTheme = staticCompositionLocalOf<Boolean> {
    error("No IsInDarkTheme defined")
}

object WalletTheme {
    val colorScheme: WalletColorScheme
        @Composable
        get() = LocalWalletColors.current
    val typography: WalletTypography
        @Composable
        get() = LocalWalletTypography.current
    val shapes: WalletShapes
        @Composable
        get() = LocalWalletShapes.current
}
