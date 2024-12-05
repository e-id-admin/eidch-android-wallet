package ch.admin.foitt.wallet.platform.deeplink.domain.usecase

interface SetDeepLinkIntent {
    operator fun invoke(data: String)
}
