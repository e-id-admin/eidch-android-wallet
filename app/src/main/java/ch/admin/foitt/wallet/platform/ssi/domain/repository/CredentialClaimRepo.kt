package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimRepositoryError
import com.github.michaelbull.result.Result

interface CredentialClaimRepo {
    suspend fun insert(credentialClaim: CredentialClaim): Result<Long, CredentialClaimRepositoryError>
    suspend fun getByCredentialId(credentialId: Long): Result<List<CredentialClaim>, CredentialClaimRepositoryError>
}
