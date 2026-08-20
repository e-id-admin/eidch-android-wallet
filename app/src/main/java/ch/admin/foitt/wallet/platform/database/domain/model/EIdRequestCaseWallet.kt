package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

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
        Index("eIdRequestCaseId"),
        Index(value = ["eIdRequestCaseId", "walletPairingId"], unique = true)
    ]
)
data class EIdRequestCaseWallet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eIdRequestCaseId: String,
    val walletPairingId: String,
    val createdAt: Long = Instant.now().epochSecond,
)
