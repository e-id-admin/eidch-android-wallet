package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class CredentialWithDisplaysAndClaims(
    @Embedded
    val credential: Credential,
    @Relation(
        entity = CredentialDisplay::class,
        parentColumn = "id",
        entityColumn = "credentialId",
    )
    val credentialDisplays: List<CredentialDisplay>,
    @Relation(
        entity = CredentialClaim::class,
        parentColumn = "id",
        entityColumn = "credentialId",
    )
    val claims: List<CredentialClaimWithDisplays>,
)
