package ch.admin.foitt.wallet.platform.activityList.domain.usecase

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetadataDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.ActivityActorDisplayWithImage

interface MapToActorMetadataDisplayData {
    suspend operator fun invoke(
        activityId: Long,
        actorDisplaysWithImages: List<ActivityActorDisplayWithImage>,
    ): ActorMetadataDisplayData
}
