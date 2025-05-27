package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.LegalRepresentativeConsent

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = EIdRequestCase::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("eIdRequestCaseId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("eIdRequestCaseId")
    ]
)
data class EIdRequestState(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eIdRequestCaseId: String,
    val state: EIdRequestQueueState,
    val lastPolled: Long,
    val onlineSessionStartOpenAt: Long? = null,
    val onlineSessionStartTimeoutAt: Long? = null,
    val legalRepresentativeConsent: LegalRepresentativeConsent = LegalRepresentativeConsent.NOT_REQUIRED,
)
