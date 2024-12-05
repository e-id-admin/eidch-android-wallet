package ch.admin.foitt.wallet.platform.deeplink.data

import ch.admin.foitt.wallet.platform.deeplink.domain.repository.DeepLinkIntentRepository
import javax.inject.Inject

class DeepLinkIntentRepositoryImpl @Inject constructor() : DeepLinkIntentRepository {
    private var currentDeepLink: String? = null

    override fun set(deepLink: String) {
        currentDeepLink = deepLink
    }

    override fun get() = currentDeepLink

    override fun reset() {
        currentDeepLink = null
    }
}
