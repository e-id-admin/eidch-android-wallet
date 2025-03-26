package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.CredentialValidity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.mapError

internal fun Credential.toAnyCredential(): Result<AnyCredential, AnyCredentialError> = runSuspendCatching {
    when (format) {
        CredentialFormat.VC_SD_JWT -> VcSdJwtCredential(
            id = id,
            keyBindingIdentifier = keyBindingIdentifier,
            keyBindingAlgorithm = if (keyBindingAlgorithm != null) SigningAlgorithm.valueOf(keyBindingAlgorithm) else null,
            payload = payload,
        )

        CredentialFormat.UNKNOWN -> error("Unsupported credential format")
    }
}.mapError { throwable ->
    throwable.toAnyCredentialError("Credential.toAnyCredential() error")
}

internal fun AnyCredential.getDisplayStatus(status: CredentialStatus): CredentialDisplayStatus {
    val validity = runSuspendCatching { validity }.getOrElse {
        return CredentialDisplayStatus.Unknown
    }
    return status.getDisplayStatus(validity)
}

private fun CredentialStatus.getDisplayStatus(validity: CredentialValidity) = when (validity) {
    // Priority is given to local state
    is CredentialValidity.Expired -> CredentialDisplayStatus.Expired(validity.expiredAt)
    is CredentialValidity.NotYetValid -> CredentialDisplayStatus.NotYetValid(validity.validFrom)
    CredentialValidity.Valid -> this.toDisplayStatus()
}

internal fun CredentialStatus.toDisplayStatus() = when (this) {
    CredentialStatus.VALID -> CredentialDisplayStatus.Valid
    CredentialStatus.REVOKED -> CredentialDisplayStatus.Revoked
    CredentialStatus.SUSPENDED -> CredentialDisplayStatus.Suspended
    CredentialStatus.UNSUPPORTED -> CredentialDisplayStatus.Unsupported
    CredentialStatus.UNKNOWN -> CredentialDisplayStatus.Unknown
}
