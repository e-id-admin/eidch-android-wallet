package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase

fun interface FetchAndCacheIssuerDisplayData {
    suspend operator fun invoke(credentialId: Long, issuer: String?)
}
