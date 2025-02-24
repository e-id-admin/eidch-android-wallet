package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

enum class EIdRequestQueueState {
    InQueueing,
    BereitFuerOnlineSession,
    InTargetWalletPairing,
    InAutoVerifikation,
    BereitFuerAnspruchspruefung,
    InAusstellung,
    Abgeschlossen,
    Abgelehnt,
    Abgebrochen,
    Abgelaufen,
}
