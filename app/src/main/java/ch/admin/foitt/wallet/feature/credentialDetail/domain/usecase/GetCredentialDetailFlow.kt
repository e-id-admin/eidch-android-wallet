package ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase

import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.CredentialDetail
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.GetCredentialDetailFlowError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetCredentialDetailFlow {
    operator fun invoke(credentialId: Long): Flow<Result<CredentialDetail?, GetCredentialDetailFlowError>>
}
