package ch.admin.foitt.wallet.feature.qrscan.presentation

sealed interface QrInfoState {
    data object Empty : QrInfoState
    data object Hint : QrInfoState
    data object Loading : QrInfoState
    data object NetworkError : QrInfoState
    data object ExpiredCredentialOffer : QrInfoState
    data object InvalidCredentialOffer : QrInfoState
    data object UnknownIssuer : QrInfoState
    data object UnknownVerifier : QrInfoState
    data object InvalidQr : QrInfoState
    data object EmptyWallet : QrInfoState
    data object NoCompatibleCredential : QrInfoState
    data object UnexpectedError : QrInfoState
    data object InvalidPresentation : QrInfoState
}
