package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.LogoUri
import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import kotlinx.serialization.Serializable

@Serializable
data class LogoUriDisplay(
    val logoUri: String,
    override val locale: String,
) : LocalizedDisplay {
    companion object {
        fun fromLogoUri(originalList: List<LogoUri>?): ArrayList<LogoUriDisplay> {
            return originalList?.mapTo(ArrayList()) { original ->
                LogoUriDisplay(
                    logoUri = original.logoUri,
                    locale = original.locale
                )
            } ?: arrayListOf()
        }
    }
}
