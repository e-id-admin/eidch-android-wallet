package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model

import androidx.compose.ui.graphics.ImageBitmap

internal sealed interface QrBoxUiState {
    data class Success(val qrBitmap: ImageBitmap) : QrBoxUiState
    object Loading : QrBoxUiState
    object Failure : QrBoxUiState
}
