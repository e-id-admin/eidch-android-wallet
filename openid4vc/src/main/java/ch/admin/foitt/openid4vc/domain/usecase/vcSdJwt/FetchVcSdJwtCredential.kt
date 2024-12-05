package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import com.github.michaelbull.result.Result

internal interface FetchVcSdJwtCredential {
    @CheckResult
    suspend operator fun invoke(
        credentialConfig: VcSdJwtCredentialConfiguration,
        credentialOffer: CredentialOffer,
    ): Result<VcSdJwtCredential, FetchCredentialError>
}
