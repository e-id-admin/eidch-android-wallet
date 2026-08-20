package ch.admin.foitt.wallet.platform.activityList.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityActorDisplayWithImageRepositoryError
import ch.admin.foitt.wallet.platform.activityList.domain.model.GetActivityActorDisplaysFlowError
import ch.admin.foitt.wallet.platform.activityList.domain.model.toGetActivityActorDisplaysFlowError
import ch.admin.foitt.wallet.platform.activityList.domain.repository.ActivityActorDisplayWithImageRepository
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.GetActivityActorDisplaysFlow
import ch.admin.foitt.wallet.platform.activityList.domain.usecase.MapToActorMetadataDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetadataDisplayData
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActivityActorDisplaysFlowImpl @Inject constructor(
    private val activityActorDisplayWithImageRepository: ActivityActorDisplayWithImageRepository,
    private val mapToActorMetadataDisplayData: MapToActorMetadataDisplayData,
) : GetActivityActorDisplaysFlow {
    override fun invoke(activityId: Long): Flow<Result<ActorMetadataDisplayData, GetActivityActorDisplaysFlowError>> =
        activityActorDisplayWithImageRepository.getActorDisplaysWithImageByActivityIdFlow(activityId)
            .mapError(ActivityActorDisplayWithImageRepositoryError::toGetActivityActorDisplaysFlowError)
            .andThen { activityActorDisplayWithImages ->
                coroutineBinding {
                    val activityActorDisplayData = mapToActorMetadataDisplayData(
                        activityId = activityId,
                        actorDisplaysWithImages = activityActorDisplayWithImages,
                    )

                    activityActorDisplayData
                }
            }
}
