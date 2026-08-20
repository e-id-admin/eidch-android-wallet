package ch.admin.foitt.wallet.platform.batch.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.BatchSize

interface GetBatchSize {
    suspend operator fun invoke(credentialId: Long): BatchSize
}
