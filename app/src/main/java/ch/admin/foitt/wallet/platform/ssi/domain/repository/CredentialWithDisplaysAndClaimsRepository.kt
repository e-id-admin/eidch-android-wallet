package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysAndClaimsRepositoryError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface CredentialWithDisplaysAndClaimsRepository {
    fun getCredentialWithDisplaysAndClaimsFlowById(
        credentialId: Long
    ): Flow<Result<CredentialWithDisplaysAndClaims, CredentialWithDisplaysAndClaimsRepositoryError>>

    fun getNullableCredentialWithDisplaysAndClaimsFlowById(
        credentialId: Long
    ): Flow<Result<CredentialWithDisplaysAndClaims?, CredentialWithDisplaysAndClaimsRepositoryError>>
}
