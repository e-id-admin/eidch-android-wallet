package ch.admin.foitt.wallet.platform.activityList.domain.usecase

import ch.admin.foitt.wallet.platform.activityList.domain.model.GetActivityActorDisplaysFlowError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetadataDisplayData
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetActivityActorDisplaysFlow {
    operator fun invoke(activityId: Long): Flow<Result<ActorMetadataDisplayData, GetActivityActorDisplaysFlowError>>
}
