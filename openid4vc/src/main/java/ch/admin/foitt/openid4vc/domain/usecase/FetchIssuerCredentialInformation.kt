package ch.admin.foitt.openid4vc.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInformationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInformation
import com.github.michaelbull.result.Result

interface FetchIssuerCredentialInformation {
    @CheckResult
    suspend operator fun invoke(
        issuerEndpoint: String,
        refresh: Boolean = false
    ): Result<IssuerCredentialInformation, FetchIssuerCredentialInformationError>
}
