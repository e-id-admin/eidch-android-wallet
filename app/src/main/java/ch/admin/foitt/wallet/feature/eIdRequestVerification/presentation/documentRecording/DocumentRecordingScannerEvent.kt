package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation.documentRecording

sealed class DocumentRecordingScannerEvent {
    object ScanDone : DocumentRecordingScannerEvent()
}
