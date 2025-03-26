package ch.admin.foitt.wallet.platform.navArgs.domain.model

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresentationSuccessNavArg(
    @SerialName("sent_fields")
    val sentFields: Array<String>,
    val issuerDisplayData: ActorDisplayData,
)
