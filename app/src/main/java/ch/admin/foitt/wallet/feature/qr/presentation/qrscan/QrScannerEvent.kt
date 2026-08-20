package ch.admin.foitt.wallet.feature.qr.presentation.qrscan

sealed class QrScannerEvent {
    object ScanSuccess : QrScannerEvent()
}
