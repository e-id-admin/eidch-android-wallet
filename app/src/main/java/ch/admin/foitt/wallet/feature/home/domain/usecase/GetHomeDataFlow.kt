package ch.admin.foitt.wallet.feature.home.domain.usecase

import ch.admin.foitt.wallet.feature.home.domain.model.GetHomeDataFlowError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetHomeDataFlow {
    operator fun invoke(): Flow<Result<List<CredentialPreview>, GetHomeDataFlowError>>
}
