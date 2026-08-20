@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.model.jwt.VerifyJwtSignatureFromDidError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.FetchCredentialStatusError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError.Unexpected
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import timber.log.Timber

interface TrustRegistryError {
    data object UnknownRegistry :
        ValidateTrustStatementError,
        ValidateTrustStatementStatusError,
        ProcessIdentityTrustStatementError,
        ValidateIdentityTrustStatementError,
        ProcessProtectedIssuanceTrustListStatementError,
        ValidateProtectedIssuanceTrustListStatementError,
        ValidateNonComplianceTrustListStatementError,
        ProcessProtectedIssuanceAuthorizationTrustStatementError,
        ValidateProtectedIssuanceAuthorizationTrustStatementError,
        ProcessVerificationAuthorizationTrustStatementError,
        ValidateVerificationAuthorizationTrustStatementError,
        IsTrustedDidError,
        ValidateVqPsError
    data object InvalidTrustStatus :
        ProcessIdentityV1TrustStatementError,
        FetchVcSchemaTrustStatusError,
        ValidateTrustStatementJwtError,
        ValidateVqPsError

    data class Unexpected(val cause: Throwable?) :
        ProcessIdentityTrustStatementError,
        ProcessIdentityV1TrustStatementError,
        FetchVcSchemaTrustStatusError,
        GetTrustDomainFromDidError,
        GetTrustUrlFromDidError,
        TrustStatementRepositoryError,
        ValidateTrustStatementError,
        ValidateTrustStatementJwtError,
        ValidateIdentityTrustStatementError,
        IsTrustedDidError,
        ValidateTrustStatementStatusError,
        ProcessProtectedIssuanceTrustListStatementError,
        ValidateProtectedIssuanceTrustListStatementError,
        ValidateNonComplianceTrustListStatementError,
        ProcessProtectedIssuanceAuthorizationTrustStatementError,
        ValidateProtectedIssuanceAuthorizationTrustStatementError,
        ValidateVqPsError,
        ProcessVerificationAuthorizationTrustStatementError,
        ValidateVerificationAuthorizationTrustStatementError
}

sealed interface ProcessIdentityTrustStatementError
sealed interface ProcessIdentityV1TrustStatementError
sealed interface FetchVcSchemaTrustStatusError
sealed interface GetTrustDomainFromDidError {
    data class NoTrustRegistryMapping(val message: String) : GetTrustDomainFromDidError
}

sealed interface GetTrustUrlFromDidError
sealed interface TrustStatementRepositoryError
sealed interface ValidateTrustStatementError
sealed interface ValidateTrustStatementJwtError
sealed interface ValidateIdentityTrustStatementError
sealed interface IsTrustedDidError
sealed interface ValidateNonComplianceTrustListStatementError
sealed interface ValidateTrustStatementStatusError
sealed interface ProcessProtectedIssuanceTrustListStatementError
sealed interface ValidateProtectedIssuanceTrustListStatementError
sealed interface ProcessProtectedIssuanceAuthorizationTrustStatementError
sealed interface ValidateProtectedIssuanceAuthorizationTrustStatementError
sealed interface ValidateVqPsError
sealed interface ProcessVerificationAuthorizationTrustStatementError
sealed interface ValidateVerificationAuthorizationTrustStatementError

fun GetTrustDomainFromDidError.toGetTrustUrlFromDidError(): GetTrustUrlFromDidError = when (this) {
    is Unexpected -> this
    is GetTrustDomainFromDidError.NoTrustRegistryMapping -> {
        Timber.w(message = this.message)
        Unexpected(null)
    }
}

fun GetTrustDomainFromDidError.toIsTrustedDidError(): IsTrustedDidError = when (this) {
    is Unexpected -> this
    is GetTrustDomainFromDidError.NoTrustRegistryMapping -> {
        Timber.w(message = this.message)
        Unexpected(null)
    }
}

fun GetTrustUrlFromDidError.toProcessIdentityV1TrustStatementError(): ProcessIdentityV1TrustStatementError = when (this) {
    is Unexpected -> this
}

fun TrustStatementRepositoryError.toProcessIdentityV1TrustStatementError(): ProcessIdentityV1TrustStatementError = when (this) {
    is Unexpected -> this
}

fun JsonParsingError.toProcessIdentityV1TrustStatementError(): ProcessIdentityV1TrustStatementError = when (this) {
    is JsonError.Unexpected -> Unexpected(throwable)
}

fun GetTrustUrlFromDidError.toFetchVcSchemaTrustStatusError(): FetchVcSchemaTrustStatusError = when (this) {
    is Unexpected -> this
}

fun TrustStatementRepositoryError.toFetchVcSchemaTrustStatusError(): FetchVcSchemaTrustStatusError = when (this) {
    is Unexpected -> this
}

fun VerifyJwtSignatureFromDidError.toValidateTrustStatementError(): ValidateTrustStatementError = when (this) {
    is JwtError.InvalidJwt,
    JwtError.DidDocumentDeactivated,
    JwtError.IssuerValidationFailed,
    JwtError.InvalidDid,
    JwtError.NetworkError -> Unexpected(null)

    is JwtError.Unexpected -> Unexpected(throwable)
}

fun VerifyJwtSignatureFromDidError.toValidateTrustStatementJwtError(): ValidateTrustStatementJwtError = when (this) {
    is JwtError.InvalidJwt,
    is JwtError.DidDocumentDeactivated,
    is JwtError.InvalidDid,
    is JwtError.IssuerValidationFailed,
    is JwtError.NetworkError -> Unexpected(null)

    is JwtError.Unexpected -> Unexpected(throwable)
}

fun Throwable.toUnexpected(message: String): Unexpected {
    Timber.w(t = this, message = message)
    return Unexpected(this)
}

fun ValidateIdentityTrustStatementError.toProcessIdentityTrustStatementError(): ProcessIdentityTrustStatementError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> this
}

fun ProcessIdentityV1TrustStatementError.toProcessIdentityTrustStatementError(): ProcessIdentityTrustStatementError = when (this) {
    is TrustRegistryError.InvalidTrustStatus -> Unexpected(null)
    is Unexpected -> this
}

fun FetchCredentialStatusError.toValidateTrustStatementStatusError(): ValidateTrustStatementStatusError = when (this) {
    is CredentialStatusError.UnknownRegistry -> TrustRegistryError.UnknownRegistry
    is CredentialStatusError.NetworkError -> Unexpected(null)
    is CredentialStatusError.Unexpected -> Unexpected(cause)
}

fun FetchCredentialStatusError.toValidateTrustStatementError(): ValidateTrustStatementError = when (this) {
    is CredentialStatusError.UnknownRegistry -> TrustRegistryError.UnknownRegistry
    is CredentialStatusError.NetworkError -> Unexpected(null)
    is CredentialStatusError.Unexpected -> Unexpected(cause)
}

fun ValidateTrustStatementJwtError.toValidateIdentityTrustStatementError(): ValidateIdentityTrustStatementError = when (this) {
    is TrustRegistryError.InvalidTrustStatus -> Unexpected(null)
    is Unexpected -> this
}

fun ValidateTrustStatementStatusError.toValidateIdentityTrustStatementError(): ValidateIdentityTrustStatementError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> this
}

fun IsTrustedDidError.toValidateIdentityTrustStatementError(): ValidateIdentityTrustStatementError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> this
}

fun IsTrustedDidError.toValidateNonComplianceTrustListStatementError(): ValidateNonComplianceTrustListStatementError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> this
}

fun IsTrustedDidError.toValidateProtectedIssuanceTrustListStatementError(): ValidateProtectedIssuanceTrustListStatementError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> this
}

fun IsTrustedDidError.toValidateProtectedIssuanceAuthorizationTrustStatementError():
    ValidateProtectedIssuanceAuthorizationTrustStatementError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> this
}

fun IsTrustedDidError.toValidateVerificationAuthorizationTrustStatementError(): ValidateVerificationAuthorizationTrustStatementError =
    when (this) {
        is TrustRegistryError.UnknownRegistry -> this
        is Unexpected -> this
    }

fun IsTrustedDidError.toValidateVqPsError(): ValidateVqPsError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> this
}

fun ValidateTrustStatementStatusError.toValidateNonComplianceTrustListStatementError(): ValidateNonComplianceTrustListStatementError =
    when (this) {
        is TrustRegistryError.UnknownRegistry -> this
        is Unexpected -> this
    }

fun ValidateTrustStatementJwtError.toValidateNonComplianceTrustListStatementError(): ValidateNonComplianceTrustListStatementError =
    when (this) {
        is TrustRegistryError.InvalidTrustStatus -> Unexpected(null)
        is Unexpected -> Unexpected(cause)
    }

fun GetTrustDomainFromDidError.toProcessProtectedIssuanceTrustListStatementError(): ProcessProtectedIssuanceTrustListStatementError =
    when (this) {
        is Unexpected -> this
        is GetTrustDomainFromDidError.NoTrustRegistryMapping -> Unexpected(IllegalStateException(message))
    }

fun TrustStatementRepositoryError.toProcessProtectedIssuanceTrustListStatementError():
    ProcessProtectedIssuanceTrustListStatementError = when (this) {
    is Unexpected -> this
}

fun ValidateProtectedIssuanceTrustListStatementError.toProcessProtectedIssuanceTrustListStatementError():
    ProcessProtectedIssuanceTrustListStatementError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> Unexpected(cause)
}

fun ValidateTrustStatementJwtError.toValidateProtectedIssuanceTrustListStatementError():
    ValidateProtectedIssuanceTrustListStatementError = when (this) {
    is TrustRegistryError.InvalidTrustStatus -> Unexpected(null)
    is Unexpected -> Unexpected(cause)
}

fun ValidateTrustStatementStatusError.toValidateProtectedIssuanceTrustListStatementError():
    ValidateProtectedIssuanceTrustListStatementError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> this
}

fun JsonParsingError.toValidateProtectedIssuanceTrustListStatementError(): ValidateProtectedIssuanceTrustListStatementError = when (this) {
    is JsonError.Unexpected -> Unexpected(throwable)
}

fun ValidateTrustStatementJwtError.toValidateProtectedIssuanceAuthorizationTrustStatementError():
    ValidateProtectedIssuanceAuthorizationTrustStatementError = when (this) {
    is TrustRegistryError.InvalidTrustStatus -> Unexpected(null)
    is Unexpected -> Unexpected(cause)
}

fun ValidateTrustStatementStatusError.toValidateProtectedIssuanceAuthorizationTrustStatementError():
    ValidateProtectedIssuanceAuthorizationTrustStatementError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> this
}

fun ValidateTrustStatementJwtError.toValidateVerificationAuthorizationTrustStatementError():
    ValidateVerificationAuthorizationTrustStatementError = when (this) {
    is TrustRegistryError.InvalidTrustStatus -> Unexpected(null)
    is Unexpected -> Unexpected(cause)
}

fun ValidateTrustStatementStatusError.toValidateVerificationAuthorizationTrustStatementError():
    ValidateVerificationAuthorizationTrustStatementError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> this
}

fun ProcessIdentityTrustStatementError.toProcessVerificationAuthorizationTrustStatementError():
    ProcessVerificationAuthorizationTrustStatementError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> this
}

fun ValidateVerificationAuthorizationTrustStatementError.toProcessVerificationAuthorizationTrustStatementError():
    ProcessVerificationAuthorizationTrustStatementError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> this
}

fun Throwable.toUnexpected(): Unexpected {
    Timber.w(t = this)
    return Unexpected(this)
}

fun JsonParsingError.toValidateProtectedIssuanceAuthorizationTrustStatementError():
    ValidateProtectedIssuanceAuthorizationTrustStatementError = when (this) {
    is JsonError.Unexpected -> Unexpected(throwable)
}

fun ValidateProtectedIssuanceAuthorizationTrustStatementError.toProcessProtectedIssuanceAuthorizationTrustStatementError():
    ProcessProtectedIssuanceAuthorizationTrustStatementError = when (this) {
    is TrustRegistryError.UnknownRegistry -> this
    is Unexpected -> Unexpected(cause)
}

fun ValidateTrustStatementJwtError.toValidateVqPsError(): ValidateVqPsError = when (this) {
    is TrustRegistryError.InvalidTrustStatus -> Unexpected(null)
    is Unexpected -> this
}

fun FetchVcSchemaTrustStatusError.toProcessProtectedIssuanceAuthorizationTrustStatementError():
    ProcessProtectedIssuanceAuthorizationTrustStatementError = when (this) {
    TrustRegistryError.InvalidTrustStatus -> Unexpected(null)
    is Unexpected -> Unexpected(cause)
}

internal fun JsonParsingError.toValidateVqPsError(): ValidateVqPsError = when (this) {
    is JsonError.Unexpected -> Unexpected(throwable)
}

inline fun <V> runOrUnexpected(block: () -> V): Result<V, Unexpected> =
    runSuspendCatching {
        block()
    }.mapError(Throwable::toUnexpected)
