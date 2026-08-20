package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActorMetadataDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetadataDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayWithImage
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import javax.inject.Inject

class MapToActorMetadataDisplayDataImpl @Inject constructor(
    private val getLocalizedDisplay: GetLocalizedDisplay,
) : MapToActorMetadataDisplayData {
    override suspend fun invoke(
        activityId: Long,
        actorDisplaysWithImages: List<ActivityActorDisplayWithImage>,
    ): ActorMetadataDisplayData {
        val actorDisplays = actorDisplaysWithImages.map { it.actorDisplay }
        val localizedActorDisplay = getLocalizedDisplay(actorDisplays)
        val imageData = actorDisplaysWithImages.find { it.actorDisplay == localizedActorDisplay }?.image?.image

        return ActorMetadataDisplayData(
            activityId = activityId,
            localizedActorName = localizedActorDisplay?.name ?: "",
            actorImageData = imageData
        )
    }
}
