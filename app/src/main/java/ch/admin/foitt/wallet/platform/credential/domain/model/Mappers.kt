package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.database.domain.model.Credential

internal fun Credential.toAnyCredential(): AnyCredential = when (format) {
    CredentialFormat.VC_SD_JWT -> VcSdJwtCredential(
        id = id,
        signingKeyId = privateKeyIdentifier,
        signingAlgorithm = if (signingAlgorithm != null) SigningAlgorithm.valueOf(signingAlgorithm) else null,
        payload = payload,
    )
    CredentialFormat.UNKNOWN -> error("Unsupported credential format")
}
