package ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase

import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOffer
import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.GetCredentialOfferFlowError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetCredentialOffer {
    operator fun invoke(credentialId: Long): Flow<Result<CredentialOffer?, GetCredentialOfferFlowError>>
}
