package ch.admin.foitt.wallet.feature.credentialDetail.domain.model

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay

data class IssuerDisplay(
    val name: String,
    val image: String? = null,
    val imageAltText: String? = null,
    override val locale: String
) : LocalizedDisplay

fun CredentialIssuerDisplay.toIssuerDisplay() = IssuerDisplay(
    name = name,
    image = image,
    imageAltText = imageAltText,
    locale = locale,
)
