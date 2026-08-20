package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentScanner

sealed class DocumentScannerEvent {
    object FirstPageDone : DocumentScannerEvent()

    object ScanDone : DocumentScannerEvent()

    object ScanFailed : DocumentScannerEvent()
}
