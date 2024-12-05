package ch.admin.foitt.wallet.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontWeight

internal val abcDiatype = FontFamily(
    Font(R.font.abc_diatype_medium, FontWeight.Medium, loadingStrategy = FontLoadingStrategy.OptionalLocal),
    Font(R.font.abc_diatype_regular, FontWeight.Normal, loadingStrategy = FontLoadingStrategy.OptionalLocal),
    Font(R.font.abc_diatype_bold, FontWeight.Bold, loadingStrategy = FontLoadingStrategy.OptionalLocal),
)

internal val abcDiatypeSemiMono = FontFamily(
    Font(R.font.abc_diatype_rounded_semimono_regular, FontWeight.Normal, loadingStrategy = FontLoadingStrategy.OptionalLocal),
)
