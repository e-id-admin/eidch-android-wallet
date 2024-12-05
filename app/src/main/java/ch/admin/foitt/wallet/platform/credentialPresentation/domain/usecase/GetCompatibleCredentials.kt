package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetCompatibleCredentialsError
import com.github.michaelbull.result.Result

fun interface GetCompatibleCredentials {
    @CheckResult
    suspend operator fun invoke(inputDescriptors: List<InputDescriptor>): Result<List<CompatibleCredential>, GetCompatibleCredentialsError>
}
