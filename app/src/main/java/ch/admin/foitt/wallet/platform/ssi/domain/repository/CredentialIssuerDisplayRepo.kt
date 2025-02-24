package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialIssuerDisplayRepositoryError
import com.github.michaelbull.result.Result

interface CredentialIssuerDisplayRepo {
    suspend fun getIssuerDisplays(credentialId: Long): Result<List<CredentialIssuerDisplay>, CredentialIssuerDisplayRepositoryError>
}
