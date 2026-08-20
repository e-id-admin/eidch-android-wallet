package ch.admin.foitt.openid4vc.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.FetchCredentialResult
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryption
import com.github.michaelbull.result.Result

internal interface FetchVerifiableCredential {
    @CheckResult
    suspend operator fun invoke(
        verifiableCredentialParams: VerifiableCredentialParams,
        credentialBindingKeyPairs: List<BindingKeyPair>?,
        dpopKeyPair: BindingKeyPair? = null,
        payloadEncryption: PayloadEncryption,
    ): Result<FetchCredentialResult, FetchVerifiableCredentialError>
}
