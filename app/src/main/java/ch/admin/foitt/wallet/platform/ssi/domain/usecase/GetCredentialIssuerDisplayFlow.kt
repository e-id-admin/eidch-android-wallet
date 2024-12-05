package ch.admin.foitt.wallet.platform.ssi.domain.usecase

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialIssuerDisplayFlowError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetCredentialIssuerDisplayFlow {
    operator fun invoke(credentialId: Long): Flow<Result<CredentialIssuerDisplay?, GetCredentialIssuerDisplayFlowError>>
}
