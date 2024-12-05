package ch.admin.foitt.openid4vc.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import com.github.michaelbull.result.Result

internal interface FetchVerifiableCredential {
    @CheckResult
    suspend operator fun invoke(
        credentialConfiguration: AnyCredentialConfiguration,
        credentialOffer: CredentialOffer,
    ): Result<VerifiableCredential, FetchVerifiableCredentialError>
}
