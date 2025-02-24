package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.CredentialValidity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrElse

internal fun Credential.toAnyCredential(): AnyCredential = when (format) {
    CredentialFormat.VC_SD_JWT -> VcSdJwtCredential(
        id = id,
        keyBindingIdentifier = keyBindingIdentifier,
        keyBindingAlgorithm = if (keyBindingAlgorithm != null) SigningAlgorithm.valueOf(keyBindingAlgorithm) else null,
        payload = payload,
    )

    CredentialFormat.UNKNOWN -> error("Unsupported credential format")
}

internal fun Credential.getDisplayStatus(): CredentialDisplayStatus {
    val validity = runSuspendCatching { toAnyCredential().validity }.getOrElse {
        return CredentialDisplayStatus.Unknown
    }
    return getDisplayStatus(validity)
}

private fun Credential.getDisplayStatus(validity: CredentialValidity) = when (validity) {
    // Priority is given to local state
    is CredentialValidity.Expired -> CredentialDisplayStatus.Expired(validity.expiredAt)
    is CredentialValidity.NotYetValid -> CredentialDisplayStatus.NotYetValid(validity.validFrom)
    CredentialValidity.Valid -> status.toDisplayStatus()
}

internal fun CredentialStatus.toDisplayStatus() = when (this) {
    CredentialStatus.VALID -> CredentialDisplayStatus.Valid
    CredentialStatus.REVOKED -> CredentialDisplayStatus.Revoked
    CredentialStatus.SUSPENDED -> CredentialDisplayStatus.Suspended
    CredentialStatus.UNSUPPORTED -> CredentialDisplayStatus.Unsupported
    CredentialStatus.UNKNOWN -> CredentialDisplayStatus.Unknown
}
