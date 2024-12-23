package ch.admin.foitt.wallet.theme

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable

object WalletTextFieldColors {

    @Composable
    fun textFieldColors() = TextFieldDefaults.colors().copy(
        focusedContainerColor = WalletTheme.colorScheme.background,
        unfocusedContainerColor = WalletTheme.colorScheme.background,
        errorContainerColor = WalletTheme.colorScheme.background,
        errorTextColor = WalletTheme.colorScheme.onSurfaceVariant,
        errorCursorColor = WalletTheme.colorScheme.onSurfaceVariant,
        errorTrailingIconColor = WalletTheme.colorScheme.onSurfaceVariant,
    )

    @Composable
    fun textFieldColorsFixed() = TextFieldDefaults.colors().copy(
        focusedTextColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        unfocusedTextColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        disabledTextColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        errorTextColor = WalletTheme.colorScheme.errorFixed,
        focusedContainerColor = WalletTheme.colorScheme.onGradientFixed,
        unfocusedContainerColor = WalletTheme.colorScheme.onGradientFixed,
        disabledContainerColor = WalletTheme.colorScheme.onGradientFixed,
        errorContainerColor = WalletTheme.colorScheme.onGradientFixed,
        cursorColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        errorCursorColor = WalletTheme.colorScheme.errorFixed,
        focusedIndicatorColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        unfocusedIndicatorColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        disabledIndicatorColor = WalletTheme.colorScheme.onSurfaceVariant,
        errorIndicatorColor = WalletTheme.colorScheme.errorFixed,
        focusedTrailingIconColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        unfocusedTrailingIconColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        disabledTrailingIconColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        errorTrailingIconColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        focusedPlaceholderColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        unfocusedPlaceholderColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        disabledPlaceholderColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        errorPlaceholderColor = WalletTheme.colorScheme.onSurfaceVariantFixed,
        focusedSupportingTextColor = WalletTheme.colorScheme.onGradientFixed,
        unfocusedSupportingTextColor = WalletTheme.colorScheme.onGradientFixed,
        disabledSupportingTextColor = WalletTheme.colorScheme.onGradientFixed,
        errorSupportingTextColor = WalletTheme.colorScheme.onGradientFixed,
        textSelectionColors = TextSelectionColors(
            handleColor = WalletTheme.colorScheme.secondaryFixed,
            backgroundColor = WalletTheme.colorScheme.secondaryFixed,
        )
    )
}
