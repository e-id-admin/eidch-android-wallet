package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import com.github.michaelbull.result.Result

interface CredentialRepo {
    suspend fun getAllIds(): Result<List<Long>, CredentialRepositoryError>
    suspend fun getAll(): Result<List<Credential>, CredentialRepositoryError>
    suspend fun getById(id: Long): Result<Credential, CredentialRepositoryError>
    suspend fun updateStatusByCredentialId(credentialId: Long, status: CredentialStatus): Result<Int, CredentialRepositoryError>
    suspend fun deleteById(id: Long): Result<Unit, CredentialRepositoryError>
}
