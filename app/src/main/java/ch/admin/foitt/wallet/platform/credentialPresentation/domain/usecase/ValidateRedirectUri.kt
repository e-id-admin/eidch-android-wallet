package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase

import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ValidateRedirectUriError
import com.github.michaelbull.result.Result

interface ValidateRedirectUri {
    operator fun invoke(uri: String): Result<Unit, ValidateRedirectUriError>
}
