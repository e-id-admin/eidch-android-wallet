package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase

import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.UpdateCredentialStatusError
import com.github.michaelbull.result.Result

interface UpdateCredentialStatus {
    suspend operator fun invoke(credentialId: Long): Result<Unit, UpdateCredentialStatusError>
}
