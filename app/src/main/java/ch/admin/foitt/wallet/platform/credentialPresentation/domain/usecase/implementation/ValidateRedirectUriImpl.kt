package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.wallet.BuildConfig
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ValidateRedirectUriError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidateRedirectUri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import javax.inject.Inject

class ValidateRedirectUriImpl @Inject constructor() : ValidateRedirectUri {
    override operator fun invoke(uri: String): Result<Unit, ValidateRedirectUriError> = binding {
        when {
            uri.startsWithPrefix(BuildConfig.SCHEME_CREDENTIAL_OFFER_SWIYU) ||
                uri.startsWithPrefix(BuildConfig.SCHEME_PRESENTATION_REQUEST_SWIYU) -> Err(
                CredentialPresentationError.InvalidUri
            ).bind()

            else -> Unit
        }
    }

    private fun String.startsWithPrefix(prefix: String) = startsWith("$prefix:")
}
