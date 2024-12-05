package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class CredentialWithIssuerAndDisplays(
    @Embedded
    val credential: Credential,
    @Relation(
        entity = CredentialDisplay::class,
        parentColumn = "id",
        entityColumn = "credentialId",
    )
    val credentialDisplays: List<CredentialDisplay>,
    @Relation(
        entity = CredentialIssuerDisplay::class,
        parentColumn = "id",
        entityColumn = "credentialId",
    )
    val issuerDisplays: List<CredentialIssuerDisplay>,
)
