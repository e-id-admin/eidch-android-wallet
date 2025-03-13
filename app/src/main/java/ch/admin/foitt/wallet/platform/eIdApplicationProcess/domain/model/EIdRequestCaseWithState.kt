package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import androidx.room.Embedded
import androidx.room.Relation
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState

data class EIdRequestCaseWithState(
    @Embedded val case: EIdRequestCase,
    @Relation(
        entity = EIdRequestState::class,
        parentColumn = "id",
        entityColumn = "eIdRequestCaseId",
    )
    val state: EIdRequestState?,
)
