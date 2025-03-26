package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData

fun interface FetchIssuerDisplayData {
    suspend operator fun invoke(credentialId: Long, issuer: String?): ActorDisplayData
}
