package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.wallet.platform.credential.domain.model.RefreshCredentialError
import com.github.michaelbull.result.Result

fun interface RefreshCredential {
    suspend operator fun invoke(credentialId: Long): Result<Unit, RefreshCredentialError>
}
