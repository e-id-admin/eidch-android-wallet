package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity
data class EIdRequestCase(
    @PrimaryKey
    val id: String, // caseId from the api
    val rawMrz: String,
    val documentNumber: String,
    val firstName: String,
    val lastName: String,
    val createdAt: Long = Instant.now().epochSecond,
)
