package ch.admin.foitt.openid4vc.domain.model

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm

data class VerifiableCredential(
    val format: CredentialFormat,
    val credential: String,
    val keyBindingIdentifier: String?,
    val keyBindingAlgorithm: SigningAlgorithm?,
)
