package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class CredentialWithDisplays(
    @Embedded val credential: Credential,
    @Relation(
        entity = CredentialDisplay::class,
        parentColumn = "id",
        entityColumn = "credentialId",
    )
    val displays: List<CredentialDisplay>,
)
