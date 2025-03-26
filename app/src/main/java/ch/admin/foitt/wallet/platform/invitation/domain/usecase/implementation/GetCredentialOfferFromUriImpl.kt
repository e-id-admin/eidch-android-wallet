package ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.wallet.platform.invitation.domain.model.GetCredentialOfferError
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.toGetCredentialOfferError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetCredentialOfferFromUri
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.toErrorIf
import java.net.URI
import java.net.URLDecoder
import javax.inject.Inject

internal class GetCredentialOfferFromUriImpl @Inject constructor(
    private val safeJson: SafeJson,
) : GetCredentialOfferFromUri {
    override fun invoke(uri: URI): Result<CredentialOffer, GetCredentialOfferError> = binding {
        val jsonString = runSuspendCatching {
            URLDecoder.decode(uri.query.split("=").last(), "UTF-8")
        }.mapError { throwable ->
            throwable.toGetCredentialOfferError("GetCredentialOfferFromUri error")
        }.bind()
        safeJson.safeDecodeStringTo<CredentialOffer>(
            string = jsonString,
        ).mapError(JsonParsingError::toGetCredentialOfferError)
            .bind()
    }.toErrorIf(predicate = { it.grants.preAuthorizedCode == null }) {
        InvitationError.UnsupportedGrantType("Unsupported grant type: ${it.grants}")
    }.toErrorIf(predicate = { it.credentialConfigurationIds.isEmpty() }) {
        InvitationError.NoCredentialsFound
    }
}
