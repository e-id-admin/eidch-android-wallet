package ch.admin.foitt.wallet.platform.database.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class CredentialClaimWithDisplays(
    @Embedded val claim: CredentialClaim,
    @Relation(
        entity = CredentialClaimDisplay::class,
        parentColumn = "id",
        entityColumn = "claimId",
    )
    val displays: List<CredentialClaimDisplay>,
)
