package ch.admin.foitt.wallet.platform.ssi.domain.usecase

import ch.admin.foitt.wallet.platform.ssi.domain.model.DeleteCredentialError
import com.github.michaelbull.result.Result

interface DeleteCredential {
    suspend operator fun invoke(credentialId: Long): Result<Unit, DeleteCredentialError>
}
