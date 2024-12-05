package ch.admin.foitt.wallet.platform.deeplink.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.deeplink.domain.repository.DeepLinkIntentRepository
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.SetDeepLinkIntent
import javax.inject.Inject

class SetDeepLinkIntentImpl @Inject constructor(
    private val deepLinkIntentRepository: DeepLinkIntentRepository,
) : SetDeepLinkIntent {
    override fun invoke(data: String) {
        deepLinkIntentRepository.set(data)
    }
}
