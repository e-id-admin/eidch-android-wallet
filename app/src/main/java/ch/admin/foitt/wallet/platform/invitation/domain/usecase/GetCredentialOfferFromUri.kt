package ch.admin.foitt.wallet.platform.invitation.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.wallet.platform.invitation.domain.model.GetCredentialOfferError
import com.github.michaelbull.result.Result
import java.net.URI

fun interface GetCredentialOfferFromUri {
    @CheckResult
    operator fun invoke(uri: URI): Result<CredentialOffer, GetCredentialOfferError>
}
