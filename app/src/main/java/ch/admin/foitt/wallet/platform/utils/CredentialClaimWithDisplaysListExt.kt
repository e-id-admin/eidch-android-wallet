package ch.admin.foitt.wallet.platform.utils

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays

/**
 * Claims with order -1 have no order defined in the metadata, so map that value to Int.MAX_VALUE to show them at the end of the list
 */
fun List<CredentialClaimWithDisplays>.sortByOrder(): List<CredentialClaimWithDisplays> = this.map {
    when (it.claim.order) {
        -1 -> CredentialClaimWithDisplays(
            claim = it.claim.copy(order = Int.MAX_VALUE),
            displays = it.displays
        )
        else -> it
    }
}.sortedBy { it.claim.order }
