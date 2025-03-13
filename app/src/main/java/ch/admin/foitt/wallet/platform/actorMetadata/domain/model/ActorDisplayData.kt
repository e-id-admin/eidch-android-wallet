package ch.admin.foitt.wallet.platform.actorMetadata.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import kotlinx.serialization.Serializable

@Serializable
data class ActorDisplayData(
    val name: List<ActorField<String>>?,
    val image: List<ActorField<String>>?,
    val preferredLanguage: String?,
    val trustStatus: TrustStatus,
    val actorType: ActorType,
) {
    companion object {
        val EMPTY by lazy {
            ActorDisplayData(
                name = listOf(),
                image = listOf(),
                preferredLanguage = null,
                trustStatus = TrustStatus.UNKNOWN,
                actorType = ActorType.UNKNOWN,
            )
        }
    }
}

@Serializable
data class ActorField<T>(
    val value: T?,
    override val locale: String,
) : LocalizedDisplay
