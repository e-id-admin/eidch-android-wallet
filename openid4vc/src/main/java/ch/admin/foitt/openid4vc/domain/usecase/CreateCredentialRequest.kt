package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.CredentialType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateCredentialRequestError
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryption
import com.github.michaelbull.result.Result

interface CreateCredentialRequest {
    suspend operator fun invoke(
        payloadEncryption: PayloadEncryption,
        credentialType: CredentialType,
    ): Result<String, CreateCredentialRequestError>
}
