package ch.admin.foitt.wallet.feature.credentialOffer.domain.model

import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError

interface CredentialOfferError {
    data class Unexpected(val throwable: Throwable?) :
        GetCredentialOfferFlowError
}

sealed interface GetCredentialOfferFlowError

fun CredentialOfferRepositoryError.toGetCredentialOfferFlowError(): GetCredentialOfferFlowError = when (this) {
    is SsiError.Unexpected -> CredentialOfferError.Unexpected(cause)
}

fun MapToCredentialClaimDataError.toGetCredentialOfferFlowError(): GetCredentialOfferFlowError = when (this) {
    is SsiError.Unexpected -> CredentialOfferError.Unexpected(cause)
}
