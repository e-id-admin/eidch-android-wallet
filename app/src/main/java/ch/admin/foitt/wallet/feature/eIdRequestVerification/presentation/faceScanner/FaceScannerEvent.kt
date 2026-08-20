package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.faceScanner

sealed class FaceScannerEvent {
    object ScanDone : FaceScannerEvent()
}
