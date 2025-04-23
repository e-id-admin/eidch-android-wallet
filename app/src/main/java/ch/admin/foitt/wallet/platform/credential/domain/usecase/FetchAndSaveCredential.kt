package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import com.github.michaelbull.result.Result

fun interface FetchAndSaveCredential {
    suspend operator fun invoke(
        credentialOffer: CredentialOffer,
    ): Result<Long, FetchCredentialError>
}
