package ch.admin.foitt.wallet.platform.navArgs.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PresentationInvalidCredentialErrorNavArg(
    val sentFields: Array<String>,
)
