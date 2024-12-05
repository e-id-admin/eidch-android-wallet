package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientName
import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import kotlinx.serialization.Serializable

@Serializable
data class ClientNameDisplay(
    val clientName: String,
    override val locale: String,
) : LocalizedDisplay {
    companion object {
        fun fromClientName(originalList: List<ClientName>?): ArrayList<ClientNameDisplay> {
            return originalList?.mapTo(ArrayList()) { original ->
                ClientNameDisplay(
                    clientName = original.clientName,
                    locale = original.locale
                )
            } ?: arrayListOf()
        }
    }
}
