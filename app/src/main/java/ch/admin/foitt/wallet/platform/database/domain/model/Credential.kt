package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import java.time.Instant

@Entity
@TypeConverters(Converters::class)
data class Credential(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val status: CredentialStatus = CredentialStatus.UNKNOWN,
    val keyBindingIdentifier: String?,
    val keyBindingAlgorithm: String?,
    val payload: String,
    val issuer: String?,
    val format: CredentialFormat,
    val validFrom: Long?,
    val validUntil: Long?,
    val createdAt: Long = Instant.now().epochSecond,
    val updatedAt: Long? = null,
)

class Converters {
    @TypeConverter
    fun toCredentialFormat(value: String) = CredentialFormat.entries.find { it.name == value } ?: CredentialFormat.UNKNOWN

    @TypeConverter
    fun fromCredentialFormat(value: CredentialFormat) = value.name
}
