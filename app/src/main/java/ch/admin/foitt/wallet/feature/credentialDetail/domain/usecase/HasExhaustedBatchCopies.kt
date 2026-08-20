package ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase

interface HasExhaustedBatchCopies {
    suspend operator fun invoke(credentialId: Long): Boolean
}
