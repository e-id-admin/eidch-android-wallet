package ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase

import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.GetCredentialIssuerDisplaysFlowError
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.IssuerDisplay
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetCredentialIssuerDisplaysFlow {
    operator fun invoke(credentialId: Long): Flow<Result<IssuerDisplay, GetCredentialIssuerDisplaysFlowError>>
}
